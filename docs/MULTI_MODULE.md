# Multi-module build and publishing

JThumbnail is built as a **Gradle multi-project** so each logical module can be **published to a Maven repository independently**. Users who only need Kafka support can depend on `jthumbnail-core` and `jthumbnail-kafka` without pulling in AMQP, gRPC, storage, etc.

## Modules

| Module | Artifact ID | Description | Depends on |
|--------|-------------|-------------|------------|
| **jthumbnail-core** | `jthumbnail-core` | API, connector contract, job processing, thumbnailers, OpenOffice | — |
| **jthumbnail-kafka** | `jthumbnail-kafka` | Kafka job queue (consumer, producer, DLQ) | core |
| **jthumbnail-webservice** | `jthumbnail-webservice` | REST API (upload, jobs, Swagger) | core |
| **jthumbnail-webhook** | `jthumbnail-webhook` | Webhook connector | core |
| **jthumbnail-amqp** | `jthumbnail-amqp` | AMQP (RabbitMQ) connector | core |
| **jthumbnail-grpc** | `jthumbnail-grpc` | gRPC server (proto + impl) | core |
| **jthumbnail-watcher** | `jthumbnail-watcher` | Filesystem watcher | core |
| **jthumbnail-storage** | `jthumbnail-storage` | S3-compatible storage connector | core |
| **jthumbnail-mcp** | `jthumbnail-mcp` | MCP server (stdio, no Spring) | (none) |
| **jthumbnail-app** | `jthumbnail-app` | Spring Boot application (all connectors + Kafka Connect sink) | core, webservice, webhook, kafka, amqp, grpc, watcher, storage |

## Build

From the project root:

```bash
./gradlew clean build
```

To skip tests (faster):

```bash
./gradlew clean build -x test
```

To build a single module:

```bash
./gradlew :jthumbnail-kafka:build
```

Output JARs (and javadoc/sources) are under each module’s `build/libs/`, e.g.:

- `jthumbnail-core/build/libs/jthumbnail-core-v2.3.0.jar`
- `jthumbnail-kafka/build/libs/jthumbnail-kafka-v2.3.0.jar`
- `jthumbnail-app/build/libs/jthumbnail-v2.3.0.jar` (executable Spring Boot JAR)

## Publishing

Each subproject has a `mavenJava` publication. To publish all modules to the configured repository (e.g. GitHub Packages or Maven Central):

```bash
./gradlew publish
```

To publish only certain modules:

```bash
./gradlew :jthumbnail-core:publish :jthumbnail-kafka:publish
```

POMs are generated with the correct dependencies (e.g. `jthumbnail-kafka` depends only on `jthumbnail-core` and Spring Kafka), so consumers get a minimal dependency set.

## Using a single connector (e.g. Kafka)

**Maven:**

```xml
<dependency>
  <groupId>io.github.makbn</groupId>
  <artifactId>jthumbnail-core</artifactId>
  <version>2.3.0</version>
</dependency>
<dependency>
  <groupId>io.github.makbn</groupId>
  <artifactId>jthumbnail-kafka</artifactId>
  <version>2.3.0</version>
</dependency>
```

**Gradle:**

```groovy
implementation 'io.github.makbn:jthumbnail-core:2.3.0'
implementation 'io.github.makbn:jthumbnail-kafka:2.3.0'
```

This does **not** pull in AMQP, gRPC, storage, webservice, webhook, watcher, or MCP.

## Running the application

The runnable application is the **jthumbnail-app** module (Spring Boot):

```bash
./gradlew :jthumbnail-app:bootRun
```

The MCP server (standalone, no Spring) can be run via:

```bash
./gradlew :jthumbnail-app:runMcpServer
```

(It uses the `jthumbnail-mcp` module classpath.)

## Tests

Tests are split across modules. Some integration tests (e.g. OpenOffice, remote office) may require a local LibreOffice/OpenOffice or remote service. Run tests with:

```bash
./gradlew test
```

To run tests for a single module:

```bash
./gradlew :jthumbnail-kafka:test
```
