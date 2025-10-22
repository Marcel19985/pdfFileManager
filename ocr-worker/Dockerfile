FROM python:3.11-slim

# Systempakete installieren: Tesseract + PDF tools
RUN apt-get update && apt-get install -y \
    tesseract-ocr ghostscript poppler-utils \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Arbeitsverzeichnis setzen
WORKDIR /app

# Python Dependencies installieren
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Worker-Skript kopieren
COPY worker.py .

# Startkommando
CMD ["python", "worker.py"]
