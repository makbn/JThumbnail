# Storage (S3 / Object Storage) Module

The storage module integrates with **S3-compatible** object storage: it reacts to S3 object creation events (via HTTP webhook or SQS), downloads the file, generates a thumbnail, and uploads the result back to the same or a different bucket with a configurable key prefix.

## Overview

- **Input:** S3 object created (or similar) events, delivered via:
  - **Webhook:** HTTP POST to a configurable path (e.g. `/storage/events`) with the event payload.
  - **SQS:** Optional SQS queue that receives S3 event notifications; the application polls the queue.
- **Flow:** Parse event → resolve bucket/key → download file to temp path → `ThumbnailJobSubmitter.submitForS3(...)` or equivalent → thumbnail uploaded to configured output location.
- **Output:** Same bucket with a prefix (e.g. `thumbnails/`) or a different bucket, configurable.

## Build

The storage module is part of the main build:

```bash
./gradlew build
```

Uses `software.amazon.awssdk:s3` (and optionally SQS). No separate artifact.

## Run

1. Enable storage and configure S3 (and optionally SQS):

   ```bash
   ./gradlew bootRun --args='--jthumbnailer.storage.enabled=true --jthumbnailer.storage.region=us-east-1'
   ```

2. For MinIO or custom endpoint:

   ```properties
   jthumbnailer.storage.enabled=true
   jthumbnailer.storage.endpoint-override=http://minio:9000
   jthumbnailer.storage.region=us-east-1
   ```

3. If using SQS for S3 events, set the queue URL:

   ```properties
   jthumbnailer.storage.sqs-queue-url=https://sqs.us-east-1.amazonaws.com/123456789/my-s3-events-queue
   ```

If `jthumbnailer.storage.enabled` is not `true`, storage beans are not registered.

## Use

### Webhook mode

Configure your S3 bucket (or bucket notification) to send events to the JThumbnail storage webhook, e.g.:

```
POST http://your-jthumbnail-host/storage/events
```

Body format is the S3 event notification JSON (or the format your `S3EventParser` expects). The application will download the object, generate the thumbnail, and upload it according to `output-strategy`, `output-prefix`, and `output-bucket`.

### SQS mode

1. Create an SQS queue and subscribe it to S3 bucket notifications (or use EventBridge).
2. Set `jthumbnailer.storage.sqs-queue-url` to that queue URL.
3. The application polls the queue and processes S3 event messages; thumbnail generation and upload follow the same logic as webhook mode.

### Bucket and file type filter

- **Bucket whitelist:** Only process events from listed buckets; empty list typically means “all”.
- **File type filter:** Optional list of allowed extensions (e.g. `pdf`, `docx`); empty may mean “all”.

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `jthumbnailer.storage.enabled` | Enable the storage connector | `false` |
| `jthumbnailer.storage.endpoint-override` | S3 endpoint (e.g. MinIO) | (none, use AWS default) |
| `jthumbnailer.storage.region` | AWS region or MinIO region | `us-east-1` |
| `jthumbnailer.storage.bucket-whitelist` | Only process these buckets; empty = all | `[]` |
| `jthumbnailer.storage.file-type-include` | Allowed extensions; empty = all | `[]` |
| `jthumbnailer.storage.output-strategy` | `SAME_BUCKET_PREFIX` or `DIFFERENT_BUCKET` | `SAME_BUCKET_PREFIX` |
| `jthumbnailer.storage.output-prefix` | Prefix for thumbnail keys | `thumbnails/` |
| `jthumbnailer.storage.output-bucket` | Target bucket when strategy is `DIFFERENT_BUCKET` | (none) |
| `jthumbnailer.storage.webhook-path` | HTTP path for S3 event webhook | `/storage/events` |
| `jthumbnailer.storage.sqs-queue-url` | SQS queue URL for S3 events (optional) | (none) |

AWS credentials: use default credential chain (env, profile, IAM role). For MinIO, set endpoint and region as above; credentials via env or profile.

Environment variables: e.g. `JTHUMBNAILER_STORAGE_ENABLED=true`, `JTHUMBNAILER_STORAGE_REGION=us-east-1`, `JTHUMBNAILER_STORAGE_SQSQUEUEURL=https://...`.

## Related documentation

- [Application](application/README.md) – Run the application
- [Connector API](connector-api/README.md) – `submitForS3` contract
