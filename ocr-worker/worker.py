import os, sys, time
import pika #rabbitMQ Python client (empfohlen von RabbitMQ)

# liest Infos aus docker-compose aus:
RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "rabbitmq")
RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", "5672"))
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "user")
RABBITMQ_PASS = os.getenv("RABBITMQ_PASS", "pass")
QUEUE_NAME    = os.getenv("QUEUE_NAME", "ocr.jobs")

def connect_with_retry(max_attempts=30, base_sleep=1.0): # falls rabbitMQ Container langsamer startet als dieser -> wird öfter probiert mit wait dazwischen
    creds = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    params = pika.ConnectionParameters(host=RABBITMQ_HOST, port=RABBITMQ_PORT, credentials=creds)
    for attempt in range(1, max_attempts+1):
        try:
            print(f"[OCR] Connecting to amqp://{RABBITMQ_USER}@{RABBITMQ_HOST}:{RABBITMQ_PORT} (attempt {attempt})", flush=True)
            return pika.BlockingConnection(params) #TCP Verbindung zu rabbitMQ Server aufbauen, synchron Modus, damit Nachrichten nacheinander abgearbeitet werden
        except Exception as e:
            sleep = min(base_sleep * attempt, 5.0) # Wartezeit steigt mit jeder Iteration aber maximal 5s warten
            print(f"[OCR] Not ready: {e} → retry in {sleep:.1f}s", flush=True)
            time.sleep(sleep)
    print("[OCR] Could not connect. Exiting.", flush=True); sys.exit(1)

def main():
    conn = connect_with_retry()
    ch = conn.channel()
    ch.queue_declare(queue=QUEUE_NAME, durable=True) #wird geschaut, ob queue schon existiert, wenn nicht wird sie erstellt
    ch.basic_qos(prefetch_count=1) #nächste Nachricht wird erst zugestellt, wenn die vorherige bestätigt wurde (acknowledged) -> verhindert Überlastung

    def on_msg(ch, method, props, body):
        try:
            print(f"[OCR] Received: {body.decode('utf-8','replace')}", flush=True)
            time.sleep(1)  # TODO: OCR
            ch.basic_ack(delivery_tag=method.delivery_tag)
            print("[OCR] Done.", flush=True)
        except Exception as e:
            print(f"[OCR] Error: {e}", flush=True)
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)

    ch.basic_consume(queue=QUEUE_NAME, on_message_callback=on_msg)
    print("[OCR] Waiting for messages...", flush=True)
    ch.start_consuming()

if __name__ == "__main__":
    main()
