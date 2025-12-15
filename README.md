# pdfFileManager
Frontend und Backend für einen PDF File Manager mit Volltextsuche der hochgeladenen Dateien und automatischer Kategorisierung.


# Programm starten
Docker Desktop starten
im Terminal des obersten Verzeichnisses eingeben: docker compose up --build

Projekt sieht man unter http://localhost/ oder http://localhost:80

# REST API Testen mit Postman
DocumentController.postman_collection.json in Postman importieren.
Im Environment eine neue variable anlegen -> Variable: baseUrl, Value: http://localhost

# Falls notwendig (Docker belegt Ports): Prozess Killen
cmd öffnen
Eingabe: netstat -ano | findstr :<port>
ganz am ende steht dann eine ID
taskkill /ID **** /F

# DB-Shell öffnen (falls die query option in IntelliJ nicht funktioniert)
in Terminal eingeben: docker exec -it $(docker ps -qf "name=postgres") psql -U test -d paperless
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