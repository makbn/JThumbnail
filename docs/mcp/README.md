# MCP Module

This module provides an **MCP (Model Context Protocol) server** so that LLM clients (e.g. Claude Desktop, Cursor) can generate thumbnails via JThumbnail using tools.

## Overview

- **Tools:** `generate_thumbnail` (submit file path or URL), `get_job_status` (query by job ID).
- **Transport:** STDIO (newline-delimited JSON-RPC). No extra MCP SDK dependency.
- **Backend:** HTTP client to a running JThumbnail instance (webhook + jobs API).

## Build

The MCP server is part of the main JThumbnail build. There is no separate artifact:

```bash
./gradlew build
```

The main class is `io.github.makbn.jthumbnail.mcp.JThumbnailMcpServer`. Use the `runMcpServer` Gradle task to run it with the correct classpath.

## Requirements

- A running **JThumbnail** instance with the **webhook** connector enabled.
- Java 21+ to run the MCP server.

## Tools

| Tool | Description |
|------|-------------|
| `generate_thumbnail` | Submit a file (local path or URL) to JThumbnail. Returns a job ID. |
| `get_job_status` | Get the status of a thumbnail job by ID (e.g. from `generate_thumbnail`). |

## Configuration

- **`JTHUMBNAIL_BASE_URL`** – Base URL of the JThumbnail service (default: `http://localhost:8081`).

Ensure JThumbnail is started with webhook enabled, for example:

```properties
jthumbnailer.webhook.enabled=true
```

## Running the MCP server

From the project root:

```bash
export JTHUMBNAIL_BASE_URL=http://localhost:8081   # optional
./gradlew runMcpServer
```

Or run the main class directly:

```bash
java -cp "build/classes/java/main:$(./gradlew -q printRuntimeClasspath 2>/dev/null || true)" \
  io.github.makbn.jthumbnail.mcp.JThumbnailMcpServer
```

A simpler option is to use the application JAR (if you build a fat JAR that includes the MCP package):

```bash
java -jar build/libs/jthumbnail-*.jar --mcp
```

If your build does not provide `--mcp`, use the `runMcpServer` Gradle task (see below).

## Gradle task

Add to `build.gradle`:

```groovy
tasks.register('runMcpServer', JavaExec) {
    group = 'application'
    description = 'Run the JThumbnail MCP server (stdio)'
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'io.github.makbn.jthumbnail.mcp.JThumbnailMcpServer'
    standardInput = System.in
    standardOutput = System.out
}
```

Then:

```bash
./gradlew runMcpServer
```

## Claude Desktop configuration

Add the MCP server to your Claude Desktop config (e.g. `~/Library/Application Support/Claude/claude_desktop_config.json` on macOS):

```json
{
  "mcpServers": {
    "jthumbnail": {
      "command": "java",
      "args": [
        "-cp", "/path/to/JThumbnail/build/classes/java/main:/path/to/your/gradle-deps",
        "io.github.makbn.jthumbnail.mcp.JThumbnailMcpServer"
      ],
      "env": {
        "JTHUMBNAIL_BASE_URL": "http://localhost:8081"
      }
    }
  }
}
```

Use a script that runs `./gradlew runMcpServer` so the classpath is correct:

```json
{
  "mcpServers": {
    "jthumbnail": {
      "command": "/absolute/path/to/JThumbnail/run-mcp.sh",
      "env": {
        "JTHUMBNAIL_BASE_URL": "http://localhost:8081"
      }
    }
  }
}
```

Where `run-mcp.sh`:

```bash
#!/usr/bin/env bash
cd "$(dirname "$0")"
exec ./gradlew runMcpServer --console=plain
```

## Protocol

The server speaks **MCP over STDIO**: newline-delimited JSON-RPC 2.0. It handles:

- `initialize` – returns server info and capabilities (tools).
- `tools/list` – returns `generate_thumbnail` and `get_job_status`.
- `tools/call` – runs the requested tool and returns text content.

No extra MCP SDK dependency is required; the implementation uses the JThumbnail HTTP API (webhook + jobs).
