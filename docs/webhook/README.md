# Webhook Module

The webhook module allows external systems (CMS, CI/CD, etc.) to trigger thumbnail jobs by **POSTing JSON** to an HTTP endpoint. JThumbnail resolves the payload to a file URL or path, creates a job, and optionally enqueues it (e.g. to Kafka).

## Overview

- **Endpoint:** `POST {contextPath}/webhook` (path configurable, default `/webhook`).
- **Payload:** JSON with a file reference. The **Generic JSON** adapter accepts `fileUrl`, `url`, or `source_url`.
- **Response:** `202 Accepted` with `jobId`, or `200` for idempotent replay, or `4xx` on error.
- **Optional:** HMAC signature validation and idempotency/replay protection.

## Build

The webhook is part of the main JThumbnail build:

```bash
./gradlew build
```

## Run

1. Start the main application with the webhook **enabled**:

   ```bash
   ./gradlew bootRun --args='--jthumbnailer.webhook.enabled=true'
   ```

2. Or set in configuration:

   ```properties
   jthumbnailer.webhook.enabled=true
   ```

If the property is not set or is `false`, the webhook controller is not registered.

## Use

### Minimal request (Generic JSON adapter)

```bash
curl -X POST http://localhost:8080/webhook \
  -H "Content-Type: application/json" \
  -d '{"fileUrl": "/absolute/path/to/file.pdf"}'
```

Or with a URL (JThumbnail will resolve it to a local path via the webhook URL resolver):

```bash
curl -X POST http://localhost:8080/webhook \
  -H "Content-Type: application/json" \
  -d '{"fileUrl": "https://example.com/document.pdf"}'
```

### Idempotency (optional)

```bash
curl -X POST http://localhost:8080/webhook \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: unique-request-id-123" \
  -d '{"fileUrl": "https://example.com/document.pdf"}'
```

### With HMAC (when secret is configured)

When `jthumbnailer.webhook.secret` is set, the server expects a signature header (e.g. `X-Webhook-Signature`). Compute the HMAC of the raw body and send it in the configured header.

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `jthumbnailer.webhook.enabled` | Enable the webhook connector | `false` |
| `jthumbnailer.webhook.path` | Request path for the webhook | `/webhook` |
| `jthumbnailer.webhook.secret` | Shared secret for HMAC validation; empty = no signature required | (none) |
| `jthumbnailer.webhook.signature-header` | Header name for HMAC signature | `X-Webhook-Signature` |
| `jthumbnailer.webhook.replay-window-seconds` | Replay window for idempotency keys | `300` |
| `jthumbnailer.webhook.idempotency-header` | Header name for idempotency key | `X-Idempotency-Key` |

Environment variables: e.g. `JTHUMBNAILER_WEBHOOK_ENABLED=true`, `JTHUMBNAILER_WEBHOOK_PATH=/webhook`.

## Adapters

Built-in adapters map incoming payloads to a `WebhookJobRequest` (fileUrl, optional idempotency key, optional source id):

- **Generic JSON** – expects `fileUrl`, `url`, or `source_url` in the JSON body.
- **WordPress** – WordPress-specific payload shape.

The first adapter that can handle the request (e.g. valid JSON with a file reference) is used. Custom adapters can be added by implementing `WebhookAdapter` and registering a Spring bean.

## Related documentation

- [Application](application/README.md) – Run the application
- [Connector API](connector-api/README.md) – Submission contract
- [MCP](mcp/README.md) – MCP server calls the webhook to submit jobs
