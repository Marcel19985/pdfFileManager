# pdfFileManager
Frontend and Backend for PDF file manager with full text search for scans

# Programm starten
Docker Desktop starten
in Terminal zu .../infra navigieren und Befehl eingeben: docker compose up -d
Applikation in IntelliJ starten (Swen3TeamMApplication)


Curl Script ausführen (doppelklicken) (davor aber PFAD anpassen (kommt später noch in eine .properties))
oder test unter maven

# DB-Shell öffnen
in Terminal eingeben: docker exec -it paperless-postgres psql -U test -d paperless
Dokument upload checken: SELECT * FROM document;
Beenden: \q

