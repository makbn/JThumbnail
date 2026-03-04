# JThumbnail Application

This document describes how to **build**, **run**, and **use** the main JThumbnail Spring Boot application.

## Overview

JThumbnail is a thumbnail generation service. The main application:

- Exposes a **REST API** for file upload and job status (when the webservice is active).
- Can consume jobs from **Kafka**, **AMQP**, **webhook**, **gRPC**, **filesystem watcher**, or **S3** (when the corresponding connector is enabled).
- Uses **OpenOffice** or **LibreOffice** for document conversion and supports many file formats (Office, PDF, images, MP3, MPEG, etc.).

## Requirements

- **Java 21** or later
- **OpenOffice 4.x** or **LibreOffice 7.x** (for document conversion), unless you use `jthumbnailer.openoffice.manager_type=none` or a remote office server
- Optional: **Kafka** (if using the job queue), **RabbitMQ** (if using AMQP), etc., depending on enabled connectors

## Build

From the project root:

```bash
./gradlew clean build
```

- Compiled classes: `build/classes/java/main`
- Test report: `build/reports/tests/test`
- JAR: `build/libs/jthumbnail-<version>.jar` (Spring Boot fat JAR if the `bootJar` task is configured; otherwise a library JAR)

To skip tests:

```bash
./gradlew clean build -x test
```

To run only unit tests (no E2E/integration):

```bash
./gradlew test
```

## Run

### Using Gradle

```bash
./gradlew bootRun
```

With custom properties:

```bash
./gradlew bootRun --args='--server.port=8081 --jthumbnailer.webhook.enabled=true'
```

### Using the JAR

After building a Spring Boot executable JAR:

```bash
java -jar build/libs/jthumbnail-*.jar
```

With environment variables:

```bash
export SERVER_PORT=8081
export JTHUMBNAILER_WEBHOOK_ENABLED=true
java -jar build/libs/jthumbnail-*.jar
```

### Configuration

All settings can be provided via:

- `application.properties` / `application.yml`
- Environment variables (e.g. `JTHUMBNAILER_SERVER_UPLOADDIRECTORY=/tmp/uploads`)
- Command-line arguments: `--jthumbnailer.webhook.enabled=true`

Key application-level properties:

| Property | Description | Default / Note |
|----------|-------------|----------------|
| `server.port` | HTTP server port | 8080 |
| `jthumbnailer.server.upload-directory` | Directory for uploaded files | Required |
| `jthumbnailer.server.max-waiting-list-size` | Max queue size for pending uploads | Required (positive) |
| `jthumbnailer.thumbnail.thumb-width` | Thumbnail width (px) | Required |
| `jthumbnailer.thumbnail.thumb-height` | Thumbnail height (px) | Required |
| `jthumbnailer.openoffice.manager_type` | `local`, `remote`, `external`, or `none` | Depends on setup |

See [Core](core/README.md), [Webservice](webservice/README.md), [Webhook](webhook/README.md), and other module READMEs for connector-specific configuration.

## Use

### Programmatic API

You can embed JThumbnail in your own Java application by starting the Spring context and using the core API:

```java
import io.github.makbn.JThumbnailerStarter;
import io.github.makbn.jthumbnail.core.JThumbnailer;
import io.github.makbn.jthumbnail.api.model.ThumbnailCandidate;
import io.github.makbn.jthumbnail.api.model.ThumbnailListener;

// Initialize (starts Spring Boot)
JThumbnailer thumbnailer = JThumbnailerStarter.init(new String[]{});

File inputFile = new File("/path/to/document.pdf");
ThumbnailCandidate candidate = new ThumbnailCandidate(inputFile, "unique_code");

thumbnailer.run(candidate, new ThumbnailListener() {
    @Override
    public void onThumbnailReady(String hash, File thumbnail) {
        // Use thumbnail file
    }

    @Override
    public void onThumbnailFailed(String hash, String message, int code) {
        // Handle failure
    }
});

// When done
thumbnailer.close();
```

### REST API (when webservice is active)

- **Upload:** `POST /` with multipart file → returns job ID.
- **Job status:** `GET /jobs/{id}`.
- **List jobs:** `GET /jobs?status=COMPLETED`.
- **Thumbnail image:** `GET /jobs/{id}/thumbnail`.
- **Swagger UI:** typically at `/swagger-ui.html`.

### Connectors

Enable one or more connectors to trigger jobs via Kafka, AMQP, webhook, gRPC, watcher, or S3. See the [Connector Specification](../CONNECTOR_SPECIFICATION.md) and the README of each module under [docs](../README.md).

## Related documentation

- [Core](core/README.md) – OpenOffice, thumbnail size, async
- [Webservice](webservice/README.md) – REST API
- [Connector API](connector-api/README.md) – Public API for building connectors
- [Root README](../../README.md) – Project overview and module index
