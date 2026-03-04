# JThumbnail Connector Specification

This document is the **public specification** for how connectors integrate with JThumbnail. It allows third-party developers to add new connectors (REST, message queues, webhooks, CMS, etc.) in a consistent, supported way.

## 1. Overview

Connectors are the entry points that **trigger** thumbnail generation from external systems. They all follow the same lifecycle:

1. **Receive** input (HTTP request, message, webhook payload, file path, etc.).
2. **Resolve** to a **local file path** (download URLs to temp files if needed).
3. **Submit** via the public API to create a job and optionally enqueue it.
4. **Processing** is handled by the core pipeline (not by the connector).

No connector implements thumbnail generation itself; they only create jobs and optionally dispatch them to the job queue.

## 2. Public API for Connectors

### 2.1 ThumbnailJobSubmitter (primary contract)

All connectors that trigger jobs **must** use the interface:

**`io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter`**

| Method | Use case |
|--------|----------|
| `submit(String localFilePath)` | Standard submission (REST upload, webhook, gRPC, AMQP, etc.). |
| `submitForWatcher(String localFilePath, String moveToProcessedDir, String moveToFailedDir)` | Filesystem watcher: move file on success/failure. |
| `submitForS3(String localFilePath, String sourceBucket, String sourceKey, String outputBucket, String outputKey)` | S3-compatible storage: thumbnail uploaded back to storage. |

- **Returns:** Job ID (never null).
- **Side effects:** Creates a persistent job; if a job queue (e.g. Kafka) is configured, enqueues the job for processing. Records metrics.

Inject `ThumbnailJobSubmitter` in your connector and call the appropriate method. You do **not** need to call `ThumbnailJobService` or `JobProducer` directly for submission.

### 2.2 When your connector processes jobs itself

Some connectors (e.g. AMQP) both receive messages and process them in the same process without Kafka. In that case:

- Create the job with `ThumbnailJobService.createJob(localFilePath)` (or the appropriate variant).
- Process it with `ThumbnailJobProcessor.process(job, maxRetries)`.
- Handle retry/DLQ according to your transport.

You still **can** use `ThumbnailJobSubmitter.submit(localFilePath)` if you want to create and enqueue to Kafka when available; otherwise create the job and process it yourself.

### 2.3 Job processing (read-only for connector authors)

Processing is done by:

- **ThumbnailJobProcessor** – runs the thumbnail pipeline (used by Kafka consumer, AMQP consumer, or in-process).
- **JobProducer** – sends a job ID to the queue (e.g. Kafka). Used internally by `ThumbnailJobSubmitter`.

Connectors do not implement or replace these.

## 3. Configuration pattern

All connectors follow the same configuration pattern:

- **Enable/disable:** `jthumbnailer.<connector>.enabled` (boolean).
- **Connector-specific:** `jthumbnailer.<connector>.<key>` (paths, queues, secrets, etc.).

Examples:

- `jthumbnailer.webhook.enabled`, `jthumbnailer.webhook.path`, `jthumbnailer.webhook.secret`
- `jthumbnailer.watcher.enabled`, `jthumbnailer.watcher.directories`
- `jthumbnailer.amqp.enabled`, `jthumbnailer.amqp.queue`
- `jthumbnailer.grpc.enabled`, `jthumbnailer.grpc.port`
- `jthumbnailer.storage.enabled` (S3)

Your connector should be gated by `@ConditionalOnProperty(name = "jthumbnailer.<your>.enabled", havingValue = "true")` and use `@ConfigurationProperties(prefix = "jthumbnailer.<your>")` for its options.

## 4. Built-in connectors (reference)

| Connector | Config prefix | Role | Submission API used |
|-----------|----------------|------|----------------------|
| **REST (webservice)** | (server) | Upload file → job | `ThumbnailJobSubmitter.submit(path)` |
| **Kafka** | `jthumbnailer.jobs` | Job queue + consumer | Jobs submitted by other connectors; consumer processes via ThumbnailJobProcessor |
| **AMQP (RabbitMQ)** | `jthumbnailer.amqp` | Consume messages, create job, process or enqueue | `ThumbnailJobService.createJob` + process or queue |
| **gRPC** | `jthumbnailer.grpc` | Streaming upload → job | `ThumbnailJobSubmitter.submit(path)` |
| **Webhook (CMS)** | `jthumbnailer.webhook` | POST JSON → job | `ThumbnailJobSubmitter.submit(path)` |
| **Filesystem watcher** | `jthumbnailer.watcher` | Watch dirs → job | `ThumbnailJobSubmitter.submitForWatcher(...)` |
| **S3 / object storage** | `jthumbnailer.storage` | S3 events → job, upload thumbnail | `ThumbnailJobSubmitter.submitForS3(...)` |

## 5. How to add a third-party connector

1. **Depend on** the public API:
   - `io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter`
   - Optionally `ThumbnailJobService`, `ThumbnailJobProcessor` if you need custom flow.

2. **Implement your trigger:** e.g. an HTTP controller, message listener, or scheduled task that receives input from your system.

3. **Resolve to a local path:** If you receive a URL, download the file to a temp path. If you receive an upload, write to a temp file. The path must be absolute and readable by the process.

4. **Submit:** Call `jobSubmitter.submit(localFilePath)` (or `submitForWatcher` / `submitForS3` if your semantics match).

5. **Return the job ID** to the client if your transport allows (e.g. HTTP 202 with `jobId` in body).

6. **Configuration:** Use `jthumbnailer.<yourconnector>.enabled` and your own properties under `jthumbnailer.<yourconnector>.*`. Register your beans only when enabled (`@ConditionalOnProperty`).

7. **Optional:** Implement a marker interface (e.g. `ThumbnailConnector`) with `getName()` for documentation and tooling; the runtime does not require it.

## 6. Package layout (for reference)

- **Public API (implement this contract):** `io.github.makbn.jthumbnail.connector.api`
  - `ThumbnailJobSubmitter` – use this to submit jobs.
- **Core (implementation details):** `io.github.makbn.jthumbnail.core.job`
  - `ThumbnailJobSubmitterImpl`, `ThumbnailJobService`, `JobProducer`, `ThumbnailJobProcessor`
- **Built-in connectors:** `webservice`, `kafka`, `amqp`, `grpc`, `webhook`, `watcher`, `storage`

Third-party connectors can live in a separate module or package; they only need to depend on the `connector.api` package and the Spring context that provides `ThumbnailJobSubmitter`.

## 7. Extending thumbnail generation (new file types)

Connectors submit jobs by file path; the **core** chooses which thumbnail generator to use by file type. To support **new file formats**, implement a custom thumbnail generator and (optionally) custom MIME detection:

- **ThumbnailProvider** (or **Thumbnailer**) – implement and register as a Spring bean; the core registry will use it when generating thumbnails.
- **MimeTypeIdentifier** – implement and register as a bean to map your file extension to the correct MIME type and output extension.

See **[EXTENDING.md](EXTENDING.md)** for step-by-step instructions and code examples.
