FROM python:3.11-slim as builder
WORKDIR /app
COPY ai-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

FROM python:3.11-slim
WORKDIR /app
COPY ai-service/. .
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port","10000"]