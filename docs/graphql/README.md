# GraphQL Connector Module

The **GraphQL** module (`jthumbnail-graphql`) exposes a GraphQL API for submitting thumbnail jobs
and querying job status/metadata, tailored for modern frontends and headless architectures.

## Overview

- **Submit job:** Mutation for creating a job from an existing local file path.
- **Query status:** Queries for a single job or jobs by status.
- **Subscribe:** Subscription that emits when a job reaches a terminal state (COMPLETED or FAILED).

The connector uses the same core pipeline as other connectors, via `ThumbnailJobSubmitter` and
`ThumbnailJobService`.

## Build

The GraphQL module is part of the main JThumbnail build:

```bash
./gradlew build
```

There is no separate artifact; the module is packaged with the main application.

## Run

Enable the GraphQL connector when starting the main application:

```bash
./gradlew bootRun --args='--jthumbnailer.graphql.enabled=true'
```

Or via configuration:

```properties
jthumbnailer.graphql.enabled=true
```

When enabled, the Spring GraphQL endpoint (e.g. `/graphql`) becomes available according to Spring
Boot’s GraphQL configuration.

## Schema

The schema (see `jthumbnail-graphql/src/main/resources/graphql/schema.graphqls`) defines:

- **Types**
  - `ThumbnailJob` – mirrors the core `ThumbnailJob` entity: `jobId`, `filePath`, `status`,
    `retryCount`, `errorMessage`, timestamps, thumbnail path, and optional S3 metadata.
  - `SubmitJobPayload` – wrapper for returned `jobId`.
  - `JobStatus` – `PENDING`, `PROCESSING`, `FAILED`, `COMPLETED`.
- **Queries**
  - `thumbnailJob(jobId: ID!): ThumbnailJob`
  - `thumbnailJobsByStatus(status: JobStatus!): [ThumbnailJob!]!`
- **Mutations**
  - `submitThumbnailJob(localFilePath: String!): SubmitJobPayload!`
- **Subscriptions**
  - `thumbnailJobCompleted(jobId: ID!): ThumbnailJob!`

## Example operations

### Submit job

```graphql
mutation {
  submitThumbnailJob(localFilePath: "/absolute/path/to/file.pdf") {
    jobId
  }
}
```

### Query job

```graphql
query {
  thumbnailJob(jobId: "job-id-123") {
    jobId
    status
    filePath
    thumbnailPath
    createdAt
    completedAt
  }
}
```

### Subscribe to completion

```graphql
subscription {
  thumbnailJobCompleted(jobId: "job-id-123") {
    jobId
    status
    thumbnailPath
  }
}
```

The subscription emits once when the job reaches a terminal state.

## Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `jthumbnailer.graphql.enabled` | Enable the GraphQL connector | `false` |

Additional GraphQL endpoint configuration (path, CORS, etc.) is controlled by standard Spring Boot
GraphQL properties.

## Related documentation

- [Application](../application/README.md) – Run the main app
- [Core](../core/README.md) – Job model and processing
- [Connector API](../connector-api/README.md) – Submission contract
- [Connector Specification](../CONNECTOR_SPECIFICATION.md) – Connector lifecycle

