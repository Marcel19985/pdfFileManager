import json
from json import JSONDecodeError
import os, sys, time
import datetime
import pika
import logging
from minio import Minio
import pytesseract
from pdf2image import convert_from_bytes

# Logging-Konfiguration
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [%(name)s] %(message)s",
    stream=sys.stdout
)
log = logging.getLogger("OCRWorker")

# RabbitMQ
RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "rabbitmq")
RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", "5672"))
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "user")
RABBITMQ_PASS = os.getenv("RABBITMQ_PASS", "pass")
QUEUE_NAME    = os.getenv("QUEUE_NAME", "ocr.jobs")
RESULT_QUEUE  = os.getenv("RESULT_QUEUE", "ocr.results")

# MinIO
MINIO_HOST = os.getenv("MINIO_HOST", "minio:9000")
MINIO_USER = os.getenv("MINIO_ROOT_USER", "minioadmin")
MINIO_PASS = os.getenv("MINIO_ROOT_PASSWORD", "minioadmin")

# Client-Verbindung zu MinIO
minio_client = Minio(
    MINIO_HOST,
    access_key=MINIO_USER,
    secret_key=MINIO_PASS,
    secure=False
)

# OCR-Funktion
def perform_ocr(document_id: str) -> str:
    """Lädt PDF aus MinIO und extrahiert Text per Tesseract."""
    log.info(f"Starting OCR for document {document_id}")

    try:
        #PDF herunterladen
        response = minio_client.get_object("documents", f"{document_id}.pdf")
        pdf_bytes = response.read()
        response.close()
        response.release_conn()

        #PDF-Seiten in Bilder umwandeln
        images = convert_from_bytes(pdf_bytes)
        log.info(f"Converted PDF to {len(images)} image(s)")

        #OCR ausführen
        text = ""
        for i, img in enumerate(images):
            page_text = pytesseract.image_to_string(img)
            text += page_text
            log.info(f"Page {i+1} OCR length={len(page_text)} chars")

        return text.strip()

    except Exception as e:
        log.exception(f"OCR processing failed for {document_id}: {e}")
        raise

# RabbitMQ-Verbindung mit Retry
def connect_with_retry(max_attempts=30, base_sleep=1.0):
    creds = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    params = pika.ConnectionParameters(host=RABBITMQ_HOST, port=RABBITMQ_PORT, credentials=creds)
    for attempt in range(1, max_attempts + 1):
        try:
            print(f"[OCR] Connecting to amqp://{RABBITMQ_USER}@{RABBITMQ_HOST}:{RABBITMQ_PORT} (attempt {attempt})", flush=True)
            return pika.BlockingConnection(params)
        except Exception as e:
            sleep = min(base_sleep * attempt, 5.0)
            log.warning(f"Not ready: {e} → retry in {sleep:.1f}s")
            time.sleep(sleep)
    log.error("Could not connect to RabbitMQ — exiting.")
    sys.exit(1)

# Verarbeitung pro Message
def on_msg(ch, method, props, body):
    # A) Parsing/Validierung – nie requeue -> verhindern von endless loop durch poison message
    try:
        data = json.loads(body) #body ist byte array -> in string umwandeln
        if not data.get("documentId"): #keine documentId vorhanden:
            raise ValueError("missing documentId")
    except JSONDecodeError as e: #ungültiges JSON:
        log.warning("Invalid JSON → drop: %r (%s)", body, e)
        ch.basic_reject(delivery_tag=method.delivery_tag, requeue=False)
        return
    except Exception as e: #sonstige Fehler bei Validierung:
        log.warning("Invalid message → drop: %r (%s)", body, e)
        ch.basic_reject(delivery_tag=method.delivery_tag, requeue=False)
        return

    # B) Verarbeitung/Publish – echte Fehler dürfen requeue (Retry)
    try:
        log.info("Received job: %s", data)
        document_id = str(data["documentId"])

        # OCR ausführen
        text = perform_ocr(document_id)

        # Ergebnis zusammenbauen
        result = {
            "documentId": document_id,
            "status": "DONE",
            "textExcerpt": text[:500],  # nur kurzer Ausschnitt
            "error": None,
            "processedAt": datetime.datetime.utcnow().isoformat() + "Z",
        }

        # Result an Queue senden
        ch.basic_publish(
            exchange="",
            routing_key=RESULT_QUEUE,
            body=json.dumps(result).encode("utf-8"),
            properties=pika.BasicProperties(content_type="application/json", delivery_mode=2),
        )
        ch.basic_ack(delivery_tag=method.delivery_tag) #Nachricht als verarbeitet markieren -> wird aus der Queue gelöscht, delivery_tag ist eindeutige ID pro Nachricht
        log.info("Done & published OCR result to %s", RESULT_QUEUE)

    except Exception as e:
        log.exception("Processing error → requeue")
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True) #Nachricht als nicht verarbeitet markieren -> wird wieder in die Queue gestellt

# Main Entry
def main():
    conn = connect_with_retry()
    ch = conn.channel()
    ch.queue_declare(queue=QUEUE_NAME, durable=True) #wird geschaut, ob queue schon existiert, wenn nicht wird sie erstellt
    ch.queue_declare(queue=RESULT_QUEUE, durable=True) #result_queue
    ch.basic_qos(prefetch_count=1) #nächste Nachricht wird erst zugestellt, wenn die vorherige bestätigt wurde (acknowledged) -> verhindert Überlastung
    ch.basic_consume(queue=QUEUE_NAME, on_message_callback=on_msg) #worker registriert sich als Konsument der queue -> on_msg wird immer aufgerufen wenn in der Queue eine Message ist
    log.info("Waiting for messages on %s → results to %s", QUEUE_NAME, RESULT_QUEUE)
    ch.start_consuming()

if __name__ == "__main__":
    main()
