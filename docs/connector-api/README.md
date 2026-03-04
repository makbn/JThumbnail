# Connector API

This module documents the **public API** that all connectors use to submit thumbnail jobs to JThumbnail. It is the single contract for REST, Kafka, AMQP, gRPC, webhook, filesystem watcher, S3, and any third-party connector.

## Overview

Connectors **trigger** thumbnail generation; they do not implement the thumbnail pipeline. They:

1. Receive input (HTTP request, message, webhook payload, file path, etc.).
2. Resolve it to a **local file path** (e.g. download a URL to a temp file).
3. Call the public API to create a job and optionally enqueue it.
4. Rely on the core pipeline to process jobs.

## Build

The connector API is part of the main JThumbnail build. There is no separate artifact.

```bash
./gradlew build
```

The public interfaces and DTOs live in:

- `io.github.makbn.jthumbnail.connector.api`

## Primary contract: ThumbnailJobSubmitter

Inject **`ThumbnailJobSubmitter`** in your connector and use one of these methods:

| Method | Use case |
|--------|----------|
| `submit(String localFilePath)` | Standard submission (REST upload, webhook, gRPC, AMQP, etc.). |
| `submitForWatcher(String localFilePath, String moveToProcessedDir, String moveToFailedDir)` | Filesystem watcher: move file on success/failure. |
| `submitForS3(String localFilePath, String sourceBucket, String sourceKey, String outputBucket, String outputKey)` | S3-compatible: thumbnail is uploaded back to storage. |

- **Returns:** Job ID (never null).
- **Side effects:** Creates a persistent job; if Kafka is configured, enqueues the job; records metrics.

### Example (standard submission)

```java
@RequiredArgsConstructor
public class MyConnector {
    private final ThumbnailJobSubmitter jobSubmitter;

    public String onFileReady(Path localFile) {
        String jobId = jobSubmitter.submit(localFile.toAbsolutePath().toString());
        return jobId;
    }
}
```

## When your connector processes jobs itself

Some connectors (e.g. AMQP) both receive messages and process them in the same process without Kafka. In that case you can:

- Create the job with **`ThumbnailJobService.createJob(localFilePath)`** (or the S3/watcher variants).
- Process it with **`ThumbnailJobProcessor.process(job, maxRetries)`**.
- Handle retries and DLQ according to your transport.

You can still use **`ThumbnailJobSubmitter.submit(localFilePath)`** if you want to create and enqueue to Kafka when available.

## Configuration pattern

All connectors follow the same pattern:

- **Enable/disable:** `jthumbnailer.<connector>.enabled` (e.g. `jthumbnailer.webhook.enabled=true`).
- **Connector-specific:** `jthumbnailer.<connector>.<key>` for paths, queues, secrets, etc.

Your connector should be gated with:

```java
@ConditionalOnProperty(name = "jthumbnailer.myconnector.enabled", havingValue = "true")
```

and use `@ConfigurationProperties(prefix = "jthumbnailer.myconnector")` for its options.

## Run / Use

The connector API is used at runtime by the main JThumbnail application. Enable the desired connectors via configuration (see each connector’s README under [docs](README.md)). There is no standalone “run” for the API; it is a dependency for connector implementations.

## Full specification

For lifecycle details, configuration rules, and step-by-step instructions to add a third-party connector, see:

- **[Connector Specification](../CONNECTOR_SPECIFICATION.md)**

To add support for **new file types** (custom thumbnail generators and MIME detection), see **[Extending JThumbnail](../EXTENDING.md)**.

## Related documentation

- [Application](application/README.md) – How to build and run the main app
- [Webservice](webservice/README.md), [Webhook](webhook/README.md), [Kafka](kafka/README.md), [AMQP](amqp/README.md), [gRPC](grpc/README.md), [Watcher](watcher/README.md), [Storage](storage/README.md) – Built-in connectors
