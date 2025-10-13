import json
from json import JSONDecodeError
import os, sys, time
import datetime
import pika #rabbitMQ Python client (empfohlen von RabbitMQ)
import logging

#config für logging (legt fest, wie log Nachrichten aussehen und wohin sie geschrieben werden)
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [%(name)s] %(message)s",
    stream=sys.stdout
)
log = logging.getLogger("OCRWorker")

# liest Infos aus docker-compose aus:
RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "rabbitmq")
RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", "5672"))
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "user")
RABBITMQ_PASS = os.getenv("RABBITMQ_PASS", "pass")
QUEUE_NAME    = os.getenv("QUEUE_NAME", "ocr.jobs")
RESULT_QUEUE  = os.getenv("RESULT_QUEUE", "ocr.results")

def connect_with_retry(max_attempts=30, base_sleep=1.0): # falls rabbitMQ Container langsamer startet als dieser -> wird öfter probiert mit wait dazwischen
    creds = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    params = pika.ConnectionParameters(host=RABBITMQ_HOST, port=RABBITMQ_PORT, credentials=creds)
    for attempt in range(1, max_attempts+1):
        try:
            print(f"[OCR] Connecting to amqp://{RABBITMQ_USER}@{RABBITMQ_HOST}:{RABBITMQ_PORT} (attempt {attempt})", flush=True)
            return pika.BlockingConnection(params) #TCP Verbindung zu rabbitMQ Server aufbauen, synchron Modus, damit Nachrichten nacheinander abgearbeitet werden
        except Exception as e:
            sleep = min(base_sleep * attempt, 5.0) # Wartezeit steigt mit jeder Iteration aber maximal 5s warten
            log.warning(f"Not ready: {e} → retry in {sleep:.1f}s")
            time.sleep(sleep)
    log.error("Could not connect to RabbitMQ — exiting.")
    sys.exit(1)

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

        time.sleep(1) # hier würde die OCR Verarbeitung stattfinden -> für Sprint 4

        # Ergebnis vorbereiten für result queue:
        result = {
            "documentId": str(data["documentId"]),
            "status": "DONE",
            "textExcerpt": "dummy text",
            "error": None,
            "processedAt": datetime.datetime.utcnow().isoformat() + "Z",
        }
        # # Ergebnis an Result-Queue senden:
        ch.basic_publish(
            exchange="",
            routing_key=RESULT_QUEUE,
            body=json.dumps(result).encode("utf-8"),
            properties=pika.BasicProperties(content_type="application/json", delivery_mode=2),
        )
        ch.basic_ack(delivery_tag=method.delivery_tag) #Nachricht als verarbeitet markieren -> wird aus der Queue gelöscht, delivery_tag ist eindeutige ID pro Nachricht
        log.info("Done & published result to %s: %s", RESULT_QUEUE, result)
    except Exception as e:
        log.exception("Processing error → requeue")
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True) #Nachricht als nicht verarbeitet markieren -> wird wieder in die Queue gestellt

def main():
    conn = connect_with_retry()
    ch = conn.channel()
    ch.queue_declare(queue=QUEUE_NAME, durable=True) #wird geschaut, ob queue schon existiert, wenn nicht wird sie erstellt
    ch.queue_declare(queue=RESULT_QUEUE, durable=True) #result_queue
    ch.basic_qos(prefetch_count=1) #nächste Nachricht wird erst zugestellt, wenn die vorherige bestätigt wurde (acknowledged) -> verhindert Überlastung
    ch.basic_consume(queue=QUEUE_NAME, on_message_callback=on_msg) #worker registriert sich als Konsument der queue -> on_msg wird immer aufgerufen wenn in der Queue eine Message ist
    log.info("Waiting for messages on %s → results to %s", QUEUE_NAME, RESULT_QUEUE)
    ch.start_consuming() #RabbitMQ jetzt bei jeder neuen Nachricht automatisch on_msg()

if __name__ == "__main__":
    main()
#