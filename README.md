# CodePilot Backend

Spring Boot backend for CodePilot, a repository-aware AI coding assistant. This service owns repository registration, repository cloning/parsing metadata, persistence, and the HTTP bridge from the React frontend to the Python FastAPI/LangGraph AI service.

## Monorepo Context

This backend is part of the CodePilot monorepo:

```text
CodePilot/
  backend/     Spring Boot API, PostgreSQL persistence, Git repository ingestion
  ai-service/  FastAPI + LangGraph repository agent
  frontend/    React + Tailwind frontend
```

Run this service from the `backend` directory.

## Tech Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- JGit
- Spring RestClient
- Maven wrapper

## Runtime Flow

1. The frontend submits a GitHub repository URL to the backend.
2. The backend stores or reuses the repository record.
3. For new or failed repositories, the backend clones the repo into a temporary local directory.
4. Source files are parsed and saved as repository file records.
5. The backend calls the AI service indexing endpoint.
6. Once indexing succeeds, the repository status becomes `READY`.
7. User questions are sent through the backend to the AI service answer endpoint.
8. The AI service returns an answer, source paths, and the conversation thread id.

## Prerequisites

- Java 21
- PostgreSQL
- Python AI service running locally or deployed
- Maven wrapper included in this folder

## Configuration

The main configuration lives in `src/main/resources/application.properties`.

Required environment variables:

```properties
DATABASE_URL=jdbc:postgresql://localhost:5432/codepilot
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password
```

AI service base URL:

```properties
spring.codepilot.ai.base-url=http://127.0.0.1:8000
```

For local development, keep the FastAPI service running at `http://127.0.0.1:8000`.

## Run Locally

From the `backend` directory:

```powershell
.\mvnw spring-boot:run
```

The backend runs on the default Spring Boot port:

```text
http://localhost:8080
```

## Run Tests

```powershell
.\mvnw test
```

## API Endpoints

### Register or Index Repository

```http
POST /api/v1/code/
Content-Type: application/json
```

Request body:

```json
{
  "url": "https://github.com/example/example-repo"
}
```

Response:

```json
{
  "id": "2c2fb2fd-86d2-4292-9c43-3340cc611866",
  "name": "example-repo",
  "url": "https://github.com/example/example-repo",
  "status": "READY",
  "errorMessage": null,
  "createdAt": "2026-09-14T00:00:00Z"
}
```

Repository statuses:

```text
QUEUED -> CLONING -> PARSING -> INDEXING -> READY
```

If processing fails, the repository is marked `FAILED`. Re-submitting a failed repository URL resets it and retries the ingestion flow.

### Ask a Repository Question

```http
POST /api/v1/repositories/questions
Content-Type: application/json
```

Request body:

```json
{
  "repo_id": "2c2fb2fd-86d2-4292-9c43-3340cc611866",
  "question": "Explain the architecture of this repository",
  "thread_id": "chat-1"
}
```

Response:

```json
{
  "answer": "The repository is structured around...",
  "sources": [
    {
      "path": "src/main/java/org/example/codepilot/CodeRepoCloning/CodeRepoService.java",
      "language": "Java"
    }
  ],
  "thread_id": "chat-1"
}
```

## AI Service Contract

The backend calls these FastAPI endpoints:

```http
POST /api/v1/ai/repositories/{repositoryId}/index
POST /api/v1/ai/answer
```

The answer request uses:

```json
{
  "repo_id": "<repository-id>",
  "question": "<user-question>",
  "thread_id": "<conversation-thread-id>"
}
```

The expected AI response includes:

```json
{
  "answer": "...",
  "sources": [
    {
      "path": "...",
      "language": "..."
    }
  ],
  "thread_id": "..."
}
```

## Development Notes

- `spring.codepilot.ai.base-url` must point to the running AI service.
- CORS is handled through Spring configuration for the React development origin.
- Temporary repository clones are created under the OS temp directory during ingestion.
- On deployment, update the AI base URL to the deployed AI runtime endpoint.
- For AWS deployment, provide database credentials through environment variables or a secrets manager instead of committing local `.env` files.

