# AMQP Module

The AMQP module allows thumbnail jobs to be **triggered by RabbitMQ** (or Azure Service Bus–compatible) messages. The connector consumes messages from a queue, maps them to a file path or URL, creates a job, and either processes it in-process or enqueues to Kafka.

## Overview

- **Exchange / queue:** Configurable exchange, queue, and routing key.
- **Consumer:** Listens on the configured queue; each message is mapped to a thumbnail job (e.g. file path or URL in the body).
- **Processing:** Jobs can be processed in the same process via `ThumbnailJobProcessor` or submitted to Kafka via `ThumbnailJobSubmitter`.
- **DLQ:** Dead-letter queue and exchange for failed messages after max retries.

## Build

AMQP is part of the main JThumbnail build:

```bash
./gradlew build
```

Requires `spring-boot-starter-amqp`. No separate artifact.

## Run

1. Enable the AMQP connector and point to RabbitMQ:

   ```bash
   ./gradlew bootRun --args='--jthumbnailer.amqp.enabled=true --spring.rabbitmq.host=localhost --spring.rabbitmq.port=5672'
   ```

2. Or in configuration:

   ```properties
   jthumbnailer.amqp.enabled=true
   spring.rabbitmq.host=localhost
   spring.rabbitmq.port=5672
   spring.rabbitmq.username=guest
   spring.rabbitmq.password=guest
   ```

If `jthumbnailer.amqp.enabled` is not `true`, the AMQP listener and related beans are not registered.

## Use

Send a message to the configured queue with a payload that the consumer can map to a file path or URL (format depends on the implementation). The consumer will:

1. Parse the message.
2. Resolve to a local file path (download URL if needed).
3. Create a job (e.g. via `ThumbnailJobSubmitter.submit(localFilePath)` or `ThumbnailJobService.createJob` and process in-process).
4. Acknowledge or reject and send to DLQ after max retries.

Exact message format (JSON, plain path, etc.) is defined by the AMQP consumer implementation in the codebase (e.g. `AmqpThumbnailConsumer`).

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `jthumbnailer.amqp.enabled` | Enable the AMQP connector | `false` |
| `jthumbnailer.amqp.exchange` | AMQP exchange name | `jthumbnail.exchange` |
| `jthumbnailer.amqp.queue` | Queue to consume | `thumbnail-jobs` |
| `jthumbnailer.amqp.routing-key` | Routing key for incoming messages | `thumbnail.request` |
| `jthumbnailer.amqp.dead-letter-queue` | DLQ name | `thumbnail-jobs.dlq` |
| `jthumbnailer.amqp.dead-letter-exchange` | DLX name | `jthumbnail.dlx` |
| `jthumbnailer.amqp.max-retries` | Max retries before DLQ | `3` |
| `jthumbnailer.amqp.consumer-concurrency` | Consumer concurrency | `1` |
| `jthumbnailer.amqp.retry-delay-ms` | Delay between retries (ms) | `30000` |
| `spring.rabbitmq.host` | RabbitMQ host | — |
| `spring.rabbitmq.port` | RabbitMQ port | 5672 |

Environment variables: e.g. `JTHUMBNAILER_AMQP_ENABLED=true`, `JTHUMBNAILER_AMQP_QUEUE=thumbnail-jobs`, `SPRING_RABBITMQ_HOST=localhost`.

## Related documentation

- [Application](application/README.md) – Run the application
- [Connector API](connector-api/README.md) – Job submission
- [Kafka](kafka/README.md) – Optional job queue after AMQP
