# Filesystem Watcher Module

The watcher module **monitors directories** for new or changed files and automatically submits them as thumbnail jobs. After processing, source files can be moved to “processed” or “failed” directories.

## Overview

- **Watched directories:** Configurable list of paths (recursive watch).
- **Stability:** Waits for file size to be stable (configurable debounce and stabilization time) to avoid partial uploads.
- **Submission:** Uses `ThumbnailJobSubmitter.submitForWatcher(localFilePath, processedDir, failedDir)` so the core can move files on success/failure.
- **Retries:** Aligned with job queue max retries.

## Build

The watcher is part of the main JThumbnail build:

```bash
./gradlew build
```

No separate artifact.

## Run

1. Enable the watcher and set at least one directory:

   ```bash
   ./gradlew bootRun --args='--jthumbnailer.watcher.enabled=true --jthumbnailer.watcher.directories[0]=/path/to/watch'
   ```

2. Or in configuration:

   ```properties
   jthumbnailer.watcher.enabled=true
   jthumbnailer.watcher.directories=/path/to/dir1,/path/to/dir2
   ```

If `jthumbnailer.watcher.enabled` is not `true`, the watcher is not started.

## Use

1. **Configure** watched directories and optional processed/failed dirs (see below).
2. **Drop files** (or write them) into the watched directories. The watcher detects stable files and submits jobs.
3. **Inspect results:** Use the REST API (`GET /jobs`, `GET /jobs/{id}`) or check the processed/failed directories.

Processed/failed paths can be relative to the watch directory or absolute. Empty or omitted values may mean “do not move” (behavior is implementation-specific).

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `jthumbnailer.watcher.enabled` | Enable the filesystem watcher | `false` |
| `jthumbnailer.watcher.directories` | List of paths to watch (recursive) | `[]` (none) |
| `jthumbnailer.watcher.poll-interval-ms` | How often to check for stable files after events | `1000` |
| `jthumbnailer.watcher.debounce-ms` | Wait after last event before considering file | `2000` |
| `jthumbnailer.watcher.stabilization-ms` | Require file size unchanged for this long (ms) | `500` |
| `jthumbnailer.watcher.max-retries` | Retries for thumbnail generation | `3` |
| `jthumbnailer.watcher.processed-dir` | Move source file here on success | `processed` |
| `jthumbnailer.watcher.failed-dir` | Move source file here on failure | `failed` |

Environment variables: e.g. `JTHUMBNAILER_WATCHER_ENABLED=true`, `JTHUMBNAILER_WATCHER_DIRECTORIES=/data/incoming`. For list properties, use indexed form (e.g. `directories[0]`) in properties files or the appropriate env binding.

## Related documentation

- [Application](application/README.md) – Run the application
- [Connector API](connector-api/README.md) – `submitForWatcher` contract
