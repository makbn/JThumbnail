# Core Module

The core module provides **thumbnail generation**, **document conversion** (OpenOffice/LibreOffice), **job lifecycle**, and **async/thread pool** configuration. It is used by all connectors and the main application.

## Overview

- **ThumbnailJobSubmitter / ThumbnailJobService / ThumbnailJobProcessor:** Create jobs, enqueue (e.g. to Kafka), and process them (convert document → generate thumbnail).
- **OpenOffice/LibreOffice:** Local, remote, or external manager for converting Office/ODT/PDF to a format suitable for thumbnailing.
- **Thumbnail dimensions and async pool:** Configurable thumbnail size and executor pool for concurrent processing.

## Build

The core is part of the main JThumbnail build. There is no separate artifact:

```bash
./gradlew build
```

## Run

The core does not run standalone. It is used by the main application (`JThumbnailerStarter`). Configure the following so the application can start and process jobs.

### Required for REST/upload

- `jthumbnailer.server.upload-directory` – directory for uploaded files
- `jthumbnailer.server.max-waiting-list-size` – max waiting list size
- `jthumbnailer.thumbnail.thumb-width` – thumbnail width (pixels)
- `jthumbnailer.thumbnail.thumb-height` – thumbnail height (pixels)

### OpenOffice / LibreOffice

One of the following must be configured so document conversion works (unless you only process images/video):

| Manager type | Property | Description |
|--------------|----------|-------------|
| **local** | `jthumbnailer.openoffice.manager_type=local` | Embedded/local OpenOffice or LibreOffice; configure `office_home`, `ports`, etc. |
| **remote** | `jthumbnailer.openoffice.manager_type=remote` | Remote LibreOffice/OpenOffice API; configure `url_connection`, etc. |
| **external** | `jthumbnailer.openoffice.manager_type=external` | External process; configure pipes/ports. |
| **none** | `jthumbnailer.openoffice.manager_type=none` | No Office conversion (e.g. image-only pipeline). |

See the property classes in `io.github.makbn.jthumbnail.core.properties` (e.g. `LocalOfficeProperties`, `RemoteOfficeProperties`) for full options. Environment variables: prefix `jthumbnailer.openoffice` with underscores and uppercase (e.g. `JTHUMBNAILER_OPENOFFICE_MANAGERTYPE=local`).

### Async (thread pool)

| Property | Description | Default |
|----------|-------------|---------|
| `jthumbnailer.async.core-pool-size` | Core pool size for async tasks | `10` |
| `jthumbnailer.async.max-pool-size` | Max pool size | `32` |

### Rate limiting (optional)

If the rate-limit filter is enabled, configure `jthumbnailer.rate-limit.*` as needed (see `RateLimitProperties` in the codebase).

### Kafka job queue (optional)

If jobs are sent to Kafka, configure `jthumbnailer.jobs.topic`, `jthumbnailer.jobs.dead-letter-topic`, and related settings. See [Kafka](kafka/README.md).

## Use

As a **user of the application**, you use the REST API, webhook, or other connectors; the core runs inside the process. As a **connector author**, you depend on `ThumbnailJobSubmitter` and optionally `ThumbnailJobService` / `ThumbnailJobProcessor` as described in the [Connector API](connector-api/README.md) and [Connector Specification](../CONNECTOR_SPECIFICATION.md).

## Configuration summary (core-related)

| Prefix / area | Purpose |
|---------------|---------|
| `jthumbnailer.server` | Upload directory, max waiting list |
| `jthumbnailer.thumbnail` | Thumb width/height |
| `jthumbnailer.openoffice` | Office manager type, ports, timeouts, office home, etc. |
| `jthumbnailer.async` | Core/max pool size |
| `jthumbnailer.rate-limit` | Rate limit (if enabled) |
| `jthumbnailer.jobs` | Kafka topic, DLQ, retries, concurrency |
| `jthumbnailer.ffmpeg` | FFmpeg for video (if used) |

## Extending: custom thumbnail generators and file types

Third parties can add support for new file types by:

- Implementing **ThumbnailProvider** (or **Thumbnailer**) and registering it as a Spring bean. The **ThumbnailProviderRegistry** picks up all such beans and tries them when generating thumbnails.
- Optionally implementing **MimeTypeIdentifier** and registering it as a bean so that custom extensions (e.g. `.xyz`) are detected with the correct MIME type and thumbnail output extension.

Provider order can be tuned with `jthumbnailer.providers.priority`.

See **[Extending JThumbnail (custom thumbnailers and MIME)](../EXTENDING.md)** for step-by-step instructions and examples.

## Related documentation

- [Application](application/README.md) – Build and run the app
- [Connector API](connector-api/README.md) – Public submission API
- [Kafka](kafka/README.md) – Job queue
- [Connector Specification](../CONNECTOR_SPECIFICATION.md) – Full connector contract
- [Extending](../EXTENDING.md) – Custom thumbnail generators and MIME detection
