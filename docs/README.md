# JThumbnail Documentation

This directory contains **module-level documentation** for JThumbnail: build, run, and use instructions for each major component.

## Module index

| Module | Description | README |
|--------|-------------|--------|
| **Application** | Main Spring Boot app: build, run, configuration, programmatic API | [application/README.md](application/README.md) |
| **Connector API** | Public API for submitting thumbnail jobs (for connector authors) | [connector-api/README.md](connector-api/README.md) |
| **Core** | Job processing, OpenOffice/LibreOffice, thumbnail size, async, rate limit | [core/README.md](core/README.md) |
| **Webservice** | REST API: upload, jobs, status, thumbnail download, Swagger | [webservice/README.md](webservice/README.md) |
| **Webhook** | HTTP webhook for CMS/CI: POST JSON with file URL to create jobs | [webhook/README.md](webhook/README.md) |
| **Kafka** | Job queue: produce/consume job IDs, DLQ, retry | [kafka/README.md](kafka/README.md) |
| **AMQP** | RabbitMQ (or compatible): consume messages, create/process jobs | [amqp/README.md](amqp/README.md) |
| **gRPC** | gRPC server for streaming/submitting thumbnail jobs | [grpc/README.md](grpc/README.md) |
| **Watcher** | Filesystem watcher: watch directories, auto-submit new files | [watcher/README.md](watcher/README.md) |
| **Storage** | S3-compatible storage: webhook/SQS events, download → thumbnail → upload | [storage/README.md](storage/README.md) |
| **MCP** | MCP server for LLM clients (Claude Desktop, Cursor): tools to generate thumbnails | [mcp/README.md](mcp/README.md) |
| **CDN Edge** | URL-based connector for CDN / signed URLs | [cdnedge/README.md](cdnedge/README.md) |
| **GraphQL** | GraphQL API for submitting jobs and querying status/metadata | [graphql/README.md](graphql/README.md) |
| **Spring Boot Starter** | Auto-configuration for embedding JThumbnail core into other Spring Boot apps | [starter/README.md](starter/README.md) |

## Other documents

- **[CONNECTOR_SPECIFICATION.md](CONNECTOR_SPECIFICATION.md)** – Connector lifecycle, configuration pattern, and how to add third-party connectors.
- **[EXTENDING.md](EXTENDING.md)** – Custom thumbnail generators and MIME detection: implement `ThumbnailProvider` or `Thumbnailer` and optionally `MimeTypeIdentifier` to support new file types.

## Quick start

1. **Build:** `./gradlew build` (from project root).
2. **Run the main app:** `./gradlew bootRun` (see [application/README.md](application/README.md) for required config).
3. **Run the MCP server** (optional): `./gradlew runMcpServer` (see [mcp/README.md](mcp/README.md)); requires a running JThumbnail instance with webhook enabled.

For a high-level project overview and supported file formats, see the [root README](../README.md).
