@echo off
setlocal

REM === Pfade/URL hier anpassen ===
set URL=http://localhost:8080/api/documents
set FILE=C:\Users\marce\Documents\Semester5\SWEN_LocalRep\swen3-teamM\testFiles\semester-project.pdf

echo Posting to %URL%
curl.exe -i --fail-with-body --verbose ^
  -X POST "%URL%" ^
  -F "file=@%FILE%;type=application/pdf" ^
  -F "title=Test Document" ^
  -F "description=Just a demo"

echo.
echo Exit code: %ERRORLEVEL%
pause
