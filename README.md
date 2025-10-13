# pdfFileManager
Frontend and Backend for PDF file manager with full text search for scans

# Programm starten
Docker Desktop starten
im Terminal des obersten Verzeichnisses eingeben: docker compose up --build

Projekt sieht man unter http://localhost/ oder http://localhost:80 (ist nämlich das Selbe)

Curl Script ausführen (doppelklicken) (davor aber PFAD anpassen (kommt später noch in eine .properties))
oder test unter maven

# Prozess Killen (VON EINEM ANDEREN PROJEKT)
cmd öffnen
Eingabe: netstat -ano | findstr :5432
ganz am ende steht dann eine ID
taskkill /ID **** /F

# DB-Shell öffnen
in Terminal eingeben: docker exec -it paperless-postgres psql -U test -d paperless
Dokument upload checken: SELECT * FROM documents;
Beenden: \q

# Maven neu builden (vor allem nach Änderungen an der pom.xml oder in docker-compose.yml)
Maven -> swen3-teamM -> Lifecycle -> clean + package

# Exception Handling 
400 – ValidationException  
404 – DocumentNotFound  
413 – UploadTooLarge  
503 – MessagingException  
500 – InternalServerError  

layer-spezifische Exceptions (Validation/Messaging/NotFound)  
RabbitTemplate mit JSON + Returns/Confirms  
Logging an persist/publish/error  

# rabbitMQ
http://localhost:15672/
User: user
Pass: pass
In Navbar unter "Queues and Streams" kann man messages publishen und Get Message(s) klicken um sie zu verarbeiten

erfolgreicher Test in ocr.jobs:
Properties: content_type = application/json
Payload:
{
"documentId": "b3e9aef1-1d32-44a3-bf8b-14cb8da67653",
"title": "Test PDF",
"createdAt": "2025-10-13T18:48:20.666Z"
}

erfolgreicher Test in ocr.results:
Properties: content_type = application/json
Payload:
{
"documentId": "123e4567-e89b-12d3-a456-426614174000",
"status": "DONE",
"textExcerpt": "dummy text",
"error": null,
"processedAt": "2025-10-13T21:25:00Z"
}

Terminal für rabbitMQ info: docker compose logs -f ocr-worker
Terminal für backend info: docker compose logs -f backend