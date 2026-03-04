# gRPC Module

The gRPC module exposes a **gRPC service** for submitting thumbnail jobs (e.g. streaming file upload). Clients can upload file data over gRPC; the server writes it to a temp file and submits a job via `ThumbnailJobSubmitter.submit(localFilePath)`.

## Overview

- **Service:** Thumbnail gRPC service (see proto definition in the project).
- **Port:** Configurable; default `9090`.
- **TLS:** Optional; can be enabled with certificate and private key paths.
- **Flow:** Client uploads file (streaming or unary) → server stores to temp file → `ThumbnailJobSubmitter.submit(path)` → returns job ID.

## Build

The gRPC module is part of the main build. Proto files are compiled by the Gradle `protobuf` plugin:

```bash
./gradlew build
```

Generated code is under `build/generated/source/proto`. No separate artifact.

## Run

1. Enable gRPC and start the application:

   ```bash
   ./gradlew bootRun --args='--jthumbnailer.grpc.enabled=true --jthumbnailer.grpc.port=9090'
   ```

2. Or in configuration:

   ```properties
   jthumbnailer.grpc.enabled=true
   jthumbnailer.grpc.port=9090
   ```

If `jthumbnailer.grpc.enabled` is not `true`, the gRPC server is not started.

## Use

Use a gRPC client (e.g. grpcurl, or a client generated from the same proto) to call the Thumbnail service. The exact RPC (e.g. `Upload` or `Submit`) and message format are defined in the project’s `.proto` file. The server responds with a job ID that can be used with the REST API (`GET /jobs/{id}`) to check status and download the thumbnail.

Example with **grpcurl** (if the service and method names match):

```bash
grpcurl -plaintext -d '{"file_path": "/path/to/file"}' localhost:9090 <package>.ThumbnailService/Submit
```

(Replace `<package>` and method name with the actual proto package and RPC name from the project.)

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `jthumbnailer.grpc.enabled` | Enable the gRPC server | `false` |
| `jthumbnailer.grpc.port` | gRPC server port | `9090` |
| `jthumbnailer.grpc.use-tls` | Use TLS | `false` |
| `jthumbnailer.grpc.cert-chain-file` | Path to PEM certificate chain (when TLS) | (none) |
| `jthumbnailer.grpc.private-key-file` | Path to PEM private key (when TLS) | (none) |

Environment variables: e.g. `JTHUMBNAILER_GRPC_ENABLED=true`, `JTHUMBNAILER_GRPC_PORT=9090`.

## Related documentation

- [Application](application/README.md) – Run the application
- [Connector API](connector-api/README.md) – Job submission contract
