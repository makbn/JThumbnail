# Kafka Module

The Kafka module provides the **job queue** for JThumbnail: jobs created by REST, webhook, gRPC, or other connectors are sent to a Kafka topic, and a consumer in this module processes them using the core thumbnail pipeline. Failed jobs can be retried and sent to a dead-letter topic.

## Overview

- **Producer:** When a connector submits a job via `ThumbnailJobSubmitter`, the job ID is sent to the configured Kafka topic (if the job queue is configured).
- **Consumer:** A Kafka listener consumes job IDs from the topic, loads the job, and runs `ThumbnailJobProcessor.process(job, maxRetries)`.
- **DLQ:** After max retries, the job can be sent to a dead-letter topic for inspection or manual retry.
- **Retry:** The REST API exposes `POST /jobs/{id}/retry` to re-queue a failed job.

## Build

Kafka support is part of the main JThumbnail build:

```bash
./gradlew build
```

Dependencies include `spring-kafka` and Kafka client. No separate artifact.

## Run

1. Ensure a Kafka broker is running and reachable.
2. Configure the job queue (see below). If `jthumbnailer.jobs.topic` and `jthumbnailer.jobs.dead-letter-topic` are not set or Kafka is not on the classpath, the application may not start or the producer/consumer may be disabled.
3. Start the application:

   ```bash
   ./gradlew bootRun --args='--spring.kafka.bootstrap-servers=localhost:9092'
   ```

The consumer subscribes to the configured topic and processes messages as they arrive.

## Use

You do not call Kafka directly from user code. Instead:

1. **Submit jobs** via any connector (REST upload, webhook, gRPC, etc.). Those connectors use `ThumbnailJobSubmitter.submit(...)`, which creates the job and, when the queue is configured, sends the job ID to Kafka.
2. **Monitor jobs** via the REST API: `GET /jobs/{id}`, `GET /jobs?status=FAILED`.
3. **Retry failed jobs** via `POST /jobs/{id}/retry` (re-sends the job ID to the main topic).

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `jthumbnailer.jobs.topic` | Kafka topic for thumbnail job IDs | Required |
| `jthumbnailer.jobs.dead-letter-topic` | Dead-letter topic for failed jobs after max retries | Required |
| `jthumbnailer.jobs.max-retries` | Max retries per job before DLQ | `3` |
| `jthumbnailer.jobs.consumer-concurrency` | Number of consumer threads | `1` |
| `spring.kafka.bootstrap-servers` | Kafka broker(s) | e.g. `localhost:9092` |
| `spring.kafka.consumer.group-id` | Consumer group for the job consumer | Set as needed |

Environment variables: e.g. `JTHUMBNAILER_JOBS_TOPIC=jthumbnail-jobs`, `JTHUMBNAILER_JOBS_DEADLETTERTOPIC=jthumbnail-jobs-dlq`, `SPRING_KAFKA_BOOTSTRAPSERVERS=localhost:9092`.

## Architecture

- **ThumbnailJobSubmitter** (or equivalent) creates a job in the database and calls **JobProducer.sendJob(jobId)**.
- **JobProducer** publishes the job ID to `jthumbnailer.jobs.topic`.
- **ThumbnailJobConsumer** listens on that topic, fetches the job by ID, and runs **ThumbnailJobProcessor.process(job, maxRetries)**. On repeated failure, the job is sent to the dead-letter topic.

## Related documentation

- [Application](application/README.md) – Run the application
- [Connector API](connector-api/README.md) – How jobs are submitted
- [Core](core/README.md) – Job processing and async settings
