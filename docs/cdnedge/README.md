# CDN Edge / URL-based connector

The **CDN Edge** module (`jthumbnail-cdnedge`) lets you create thumbnail jobs directly from
public or signed URLs (CDN-backed content) instead of uploading files.

## Use cases

- Headless CMS and media pipelines that work with CDN URLs
- Pre-signed URLs from cloud storage
- Serverless / edge architectures where files are not stored locally

## How it works

1. Client sends a POST request with a URL to the CDN Edge API.
2. The connector downloads the asset to a local temp file with safety checks:
   - Allowed hosts and extensions
   - HTTP/HTTPS only
   - Max size limit
   - Download timeout
3. The connector submits the local file path via `ThumbnailJobSubmitter.submit(...)`.
4. The core pipeline generates a thumbnail; existing job APIs (webservice, DB, etc.)
   can be used to query status and fetch results.

## Configuration

Enable the connector and configure basic safety limits:

```properties
jthumbnailer.cdnedge.enabled=true

# Optional: restrict which hosts can be used
jthumbnailer.cdnedge.allowed-hosts=cdn.example.com,images.example.org

# Optional: restrict file extensions
jthumbnailer.cdnedge.allowed-extensions=jpg,jpeg,png,gif,pdf

# Max download size in bytes (default: 50MB)
jthumbnailer.cdnedge.max-bytes=52428800

# Download timeout
jthumbnailer.cdnedge.download-timeout=PT30S
```

## API

### Create job from URL

```http
POST /api/cdnedge/jobs
Content-Type: application/json

{
  "url": "https://cdn.example.com/assets/file.pdf"
}
```

**Response**

```json
{
  "jobId": "c0a80123-4567-89ab-cdef-0123456789ab"
}
```

Use the existing job inspection endpoints from the webservice module to poll status
and obtain the resulting thumbnail.

## Implementation details

- `CdnEdgeDownloadService`
  - Validates scheme (`http`/`https`), host, and file extension.
  - Enforces max-bytes via `Content-Length` and a hard limit while streaming.
  - Writes to a temporary file and returns its `File` reference.
- `CdnEdgeJobController`
  - `POST /api/cdnedge/jobs`
  - Uses `CdnEdgeDownloadService` and `ThumbnailJobSubmitter.submit(...)`.

This module follows the standard connector pattern described in
`docs/CONNECTOR_SPECIFICATION.md`.

