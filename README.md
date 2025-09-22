# pdfFileManager
Frontend and Backend for PDF file manager with full text search for scans

# Programm starten
Docker Desktop starten
im Ordner swen3-teamM Befehl eingeben: docker compose up --build

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
