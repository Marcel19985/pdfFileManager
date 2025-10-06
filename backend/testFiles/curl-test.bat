@echo off
setlocal enabledelayedexpansion

:: ============================================
:: CONFIG
:: ============================================
set URL=http://localhost:8081/api/documents
set FILE=C:\Users\marce\Documents\Semester5\SWEN_LocalRepNew\backend\testFiles\semester-project.pdf

set UPDATED_TITLE=Updated Title
set UPDATED_DESC=Updated description via EDIT test

:: ============================================
:: POST (Create)
:: ============================================
echo Creating new document ...
for /f "tokens=*" %%i in ('curl.exe -s -X POST "%URL%" -F "file=@%FILE%;type=application/pdf" -F "title=Test Document" -F "description=Just a demo"') do (
    set RESPONSE=%%i
)
echo Response: !RESPONSE!

:: === ID aus JSON extrahieren ===
for /f "tokens=2 delims=:" %%a in ('echo !RESPONSE! ^| findstr /i ""id""') do (
    set DOC_ID=%%a
)
set DOC_ID=!DOC_ID:"=!
for /f "tokens=1 delims=," %%b in ("!DOC_ID!") do set DOC_ID=%%b
set DOC_ID=!DOC_ID: =!
echo Extracted DOC_ID: !DOC_ID!

:: ============================================
:: GET all documents
:: ============================================
echo.
echo Getting all documents ...
curl.exe -i -X GET "%URL%"
echo.

:: ============================================
:: GET document by ID
:: ============================================
echo.
echo Getting document by ID ...
curl.exe -i -X GET "%URL%/!DOC_ID!"
echo.

:: ============================================
:: EDIT (PUT MULTIPART) – Titel/Beschreibung aktualisieren
:: ============================================
echo.
echo Editing document by ID ...
curl.exe -i -X PUT "%URL%/!DOC_ID!" ^
  -F "title=%UPDATED_TITLE%" ^
  -F "description=%UPDATED_DESC%"
echo.

:: ============================================
:: GET document by ID (nach Update)
:: ============================================
echo.
echo Verifying updated document ...
curl.exe -i -X GET "%URL%/!DOC_ID!"
echo.

:: ============================================
:: OPTIONAL: DELETE document by ID
:: ============================================
echo.
echo Deleting document by ID ...
curl.exe -i -X DELETE "%URL%/!DOC_ID!"
echo.

pause
