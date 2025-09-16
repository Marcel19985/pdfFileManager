# pdfFileManager
Frontend and Backend for PDF file manager with full text search for scans

# Programm starten
Docker Desktop starten
in Terminal zu .../infra navigieren und Befehl eingeben: docker compose up -d
Applikation in IntelliJ starten
Curl Script ausführen

Um DB-Shell zu öffnen:
in Terminal eingeben: docker exec -it paperless-postgres psql -U test -d paperless
Dokument upload checken: SELECT * FROM document;
Beenden: \q

