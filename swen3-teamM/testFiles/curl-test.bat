@echo off
setlocal enabledelayedexpansion

set URL=http://localhost:8081/api/documents
set FILE=C:\FH Technikum\Semester 4\Software Emgineering 2\pdfFileManager\swen3-teamM\testFiles\semester-project.pdf

REM === POST ===
for /f "tokens=*" %%i in ('curl.exe -s -X POST "%URL%" -F "file=@%FILE%;type=application/pdf" -F "title=Test Document" -F "description=Just a demo"') do (
    set RESPONSE=%%i
)

echo Response: !RESPONSE!

REM === ID aus JSON extrahieren robust ===
for /f "tokens=2 delims=:" %%a in ('echo !RESPONSE! ^| findstr /i ""id""') do (
    set DOC_ID=%%a
)

REM Nur die UUID behalten, Anführungszeichen entfernen und evtl. Komma
set DOC_ID=!DOC_ID:"=!
for /f "tokens=1 delims=," %%b in ("!DOC_ID!") do set DOC_ID=%%b

echo Extracted DOC_ID: !DOC_ID!

REM === GET all documents ===
echo.
echo Getting all documents
curl.exe -i -X GET "%URL%"
echo.

REM === GET document by ID ===
echo.
echo Getting document by ID
curl.exe -i -X GET "%URL%/!DOC_ID!"
echo.


REM === DELETE document by ID ===
::echo.
::echo Deleting document by ID
::curl.exe -i -X DELETE "%URL%/!DOC_ID!"
::echo.

pause
