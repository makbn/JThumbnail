# Webservice (REST API) Module

The webservice module provides the **REST API** for uploading files, querying job status, and downloading thumbnails. It is part of the main JThumbnail application and is active whenever the application runs (no separate “enabled” flag for the server itself).

## Overview

- **File upload:** `POST /` with multipart form data → creates a thumbnail job and returns job ID.
- **Job by ID:** `GET /jobs/{id}` → job status, thumbnail path, error message.
- **List jobs:** `GET /jobs?status=COMPLETED` (optional filter).
- **Thumbnail image:** `GET /jobs/{id}/thumbnail` → returns the generated image (JPEG/PNG).
- **Retry failed job:** `POST /jobs/{id}/retry` (when Kafka is configured).
- **Admin:** `GET /admin` (if configured).
- **OpenAPI / Swagger UI:** API docs and interactive UI (path depends on Springdoc config).

## Build

The webservice is part of the single JThumbnail Gradle project. Build the whole application:

```bash
./gradlew build
```

No separate build step is required for the webservice module.

## Run

Start the main application; the REST server starts on the configured port (default 8080):

```bash
./gradlew bootRun
```

Or with a custom port:

```bash
./gradlew bootRun --args='--server.port=8081'
```

Required configuration for the server and uploads (see [Core](core/README.md) and [Application](application/README.md)):

- `jthumbnailer.server.upload-directory` – directory for uploaded files
- `jthumbnailer.server.max-waiting-list-size` – max queue size

## Use

### Upload a file

```bash
curl -X POST http://localhost:8080/ \
  -F "file=@/path/to/document.pdf"
```

Response (e.g. JSON) includes the job ID. Use it to poll status or fetch the thumbnail.

### Get job status

```bash
curl http://localhost:8080/jobs/{jobId}
```

### Get thumbnail image

```bash
curl -o thumbnail.png "http://localhost:8080/jobs/{jobId}/thumbnail"
```

### List jobs by status

```bash
curl "http://localhost:8080/jobs?status=COMPLETED"
```

### Retry a failed job (requires Kafka)

```bash
curl -X POST http://localhost:8080/jobs/{jobId}/retry
```

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | HTTP server port | 8080 |
| `jthumbnailer.server.upload-directory` | Directory for multipart uploads | Required |
| `jthumbnailer.server.max-waiting-list-size` | Max size of waiting list for uploads | Required |
| `spring.servlet.multipart.max-file-size` | Max upload file size | Spring default |
| `spring.servlet.multipart.max-request-size` | Max request size | Spring default |
| `jthumbnailer.rate-limit.*` | Rate limiting (if enabled) | See Core / config |
| `springdoc.api-docs.path` | OpenAPI JSON path | Springdoc default |
| `springdoc.swagger-ui.path` | Swagger UI path | Springdoc default |

All parameters can be set via environment variables (e.g. `SERVER_PORT`, `JTHUMBNAILER_SERVER_UPLOADDIRECTORY`). See the root [README](../../README.md) for the naming convention.

## Related documentation

- [Application](application/README.md) – Build and run the app
- [Core](core/README.md) – Server and thumbnail properties
- [Connector API](connector-api/README.md) – How uploads create jobs via `ThumbnailJobSubmitter`
