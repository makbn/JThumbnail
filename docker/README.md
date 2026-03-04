# Docker / Compose deployment

This directory contains Docker and Docker Compose definitions for running **JThumbnail** in
different scenarios. All images share a common base that includes:

- Java **21** (Temurin JRE)
- **LibreOffice** (for document conversion)
- **FFmpeg** (for video thumbnails)

The application uses H2 file storage by default and can be combined with Kafka, MinIO (S3), gRPC,
webhook, CDN Edge, and GraphQL connectors via configuration.

## 1. Build the application JAR

From the project root, build the Spring Boot application JAR:

```bash
./gradlew :jthumbnail-app:bootJar
```

This produces `build/libs/*.jar`, which the Dockerfile expects.

## 2. Build the Docker image

From the project root:

```bash
docker build -f docker/Dockerfile.app -t jthumbnail:latest .
```

The image:

- Uses LibreOffice + FFmpeg for full document and video support.
- Exposes:
  - `8081` – REST API, webservice, GraphQL, webhooks
  - `9090` – gRPC (when enabled)

You can override JVM options via `JAVA_OPTS`.

## 3. Base stack: REST + GraphQL + H2 (single container)

Use the default compose file:

```bash
docker compose -f docker/docker-compose.yml up --build
```

This starts:

- `jthumbnail-app` (built from `Dockerfile.app`)
  - H2 file DB at `/data/jthumbnail`
  - Upload directory at `/data/upload`
  - Volumes:
    - `./data` → `/data`
    - `./logs` → `/logs`

Environment highlights (see `docker-compose.yml`):

- `SPRING_DATASOURCE_URL=jdbc:h2:file:/data/jthumbnail;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- `JTHUMBNAILER_SERVER_UPLOADDIRECTORY=/data/upload`
- `JTHUMBNAILER_THUMBNAIL_THUMBWIDTH` / `THUMBHEIGHT`
- Example connectors toggled on:
  - `JTHUMBNAILER_GRAPHQL_ENABLED=true`
  - `JTHUMBNAILER_CDNEDGE_ENABLED=true`

### Access

- REST / webservice: `http://localhost:8081/`
- Jobs API (see `docs/webservice/README.md`): `http://localhost:8081/jobs/{id}`
- GraphQL (Spring default): `http://localhost:8081/graphql`
- Actuator (health/metrics): `http://localhost:8081/actuator/health`

## 4. Kafka job queue scenario

For production-style async processing with Kafka, combine the base stack with `docker-compose.kafka.yml`:

```bash
docker compose \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.kafka.yml \
  up --build
```

This adds:

- `zookeeper` (Confluent image)
- `kafka` (Confluent image)

And configures the app with:

- `SPRING_KAFKA_BOOTSTRAPSERVERS=kafka:9092`
- `JTHUMBNAILER_JOBS_TOPIC=thumbnail-jobs`
- `JTHUMBNAILER_JOBS_DEADLETTERTOPIC=thumbnail-jobs-dlq`
- `JTHUMBNAILER_JOBS_MAXRETRIES=3`
- `JTHUMBNAILER_JOBS_CONSUMERCONCURRENCY=1`

**Usage pattern:**

1. Upload or submit a job via REST/webhook/GraphQL/gRPC.
2. Job is persisted and the job ID is sent to Kafka.
3. The Kafka consumer in the app processes the job via `ThumbnailJobProcessor`.
4. Status and thumbnails are available via the REST/GraphQL APIs.

## 5. S3 / MinIO storage scenario

To process object storage events and upload thumbnails back to S3/MinIO, add
`docker-compose.storage.yml`:

```bash
docker compose \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.storage.yml \
  up --build
```

This adds:

- `minio` – S3-compatible storage (port `9000`, console at `9001`)

And enables the storage connector in the app:

- `JTHUMBNAILER_STORAGE_ENABLED=true`
- `JTHUMBNAILER_STORAGE_ENDPOINTOVERRIDE=http://minio:9000`
- `JTHUMBNAILER_STORAGE_REGION=us-east-1`

You can configure bucket whitelist and file type filters via:

- `JTHUMBNAILER_STORAGE_BUCKETWHITELIST`
- `JTHUMBNAILER_STORAGE_FILETYPEINCLUDE`

See `docs/storage/README.md` for how to wire S3/MinIO events (webhook or SQS) into JThumbnail.

## 6. Other connector scenarios

All connectors can be toggled using environment variables following the existing configuration
pattern. Common examples:

- **Webhook connector**
  - Enable:
    - `JTHUMBNAILER_WEBHOOK_ENABLED=true`
    - `JTHUMBNAILER_WEBHOOK_PATH=/webhook`
  - Optionally set:
    - `JTHUMBNAILER_WEBHOOK_SECRET` for HMAC
    - `JTHUMBNAILER_WEBHOOK_SIGNATUREHEADER`
    - `JTHUMBNAILER_WEBHOOK_IDEMPOTENCYHEADER`

- **gRPC**
  - `JTHUMBNAILER_GRPC_ENABLED=true`
  - `JTHUMBNAILER_GRPC_PORT=9090`
  - Optional TLS settings (see `docs/grpc/README.md`).

- **Filesystem watcher**
  - `JTHUMBNAILER_WATCHER_ENABLED=true`
  - `JTHUMBNAILER_WATCHER_DIRECTORIES=/data/watch`

- **CDN Edge**
  - `JTHUMBNAILER_CDNEDGE_ENABLED=true`
  - `JTHUMBNAILER_CDNEDGE_ALLOWEDHOSTS=cdn.example.com,images.example.org`
  - `JTHUMBNAILER_CDNEDGE_ALLOWEDEXTENSIONS=jpg,jpeg,png,gif,pdf`

- **GraphQL**
  - `JTHUMBNAILER_GRAPHQL_ENABLED=true`
  - Use `/graphql` for queries/mutations/subscriptions.

All of these can be added under the `environment:` section of `jthumbnail-app` in your chosen
compose file.

## 7. Recommended workflows

- **Local development (simple):**
  - Use `docker/docker-compose.yml` only.
  - H2 DB + REST + GraphQL + CDN Edge; no Kafka or external services.

- **Integration testing with Kafka:**
  - Use `docker/docker-compose.yml` + `docker/docker-compose.kafka.yml`.
  - Run REST/webhook/GraphQL clients against `localhost:8081`.

- **Storage pipeline (S3/MinIO):**
  - Use all three files:
    - `docker/docker-compose.yml`
    - `docker/docker-compose.kafka.yml` (optional but recommended)
    - `docker/docker-compose.storage.yml`
  - Configure MinIO buckets and event notifications to point at the storage webhook.

## 8. Notes

- The provided compose files assume you **build the application JAR first** using Gradle.
- For production, you should:
  - Use a tagged image (`jthumbnail:<version>`).
  - Externalize configuration via environment or configuration server.
  - Attach proper volumes for `/data` and `/logs`.

Refer to the individual module READMEs under `docs/` for detailed API and configuration options per
connector.

