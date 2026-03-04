## JThumbnail

JThumbnail is a Java library for creating Thumbnails of common types of file including `.doc`, `.docx`, `.pdf` , `.mp4` etc. [full list](#supported-file-formats)

- **Project is under development!**
- Check the `v1` branch for Java 8 compatible version. 
- Check the `dev/**` branches for latest commits on different under development version.
- Check the `release/**` branches for stable release versions
- `master` branch will always point to the latest available release version

Project Source: https://github.com/makbn/JThumbnail

## How to use

```jshelllanguage
String[] args = new String[]{};

JThumbnailer jThumbnailer = JThumbnailerStarter.init(args);

File in = new File("/inputFile.docx");

// Minimal usage – uses default engine configuration
ThumbnailCandidate candidate = ThumbnailCandidate.of(in, "unique_code");

// Optional: override output format, size, cropping, rotation, color, border, text, ...
ThumbnailConfig config = ThumbnailConfig.builder()
        .outputFormat(ThumbnailConfig.OutputFormat.WEBP)
        .width(512)
        .height(512)
        .cropMode(ThumbnailConfig.CropMode.FILL) // center-crop to fill box
        .rotationDegrees(0.0)
        .colorFilter(ThumbnailConfig.ColorFilter.SEPIA)
        .addBorder(true)
        .borderSize(4)
        .borderColor("#000000")
        .overlayText("Sample")
        .overlayTextSize(18)
        .overlayTextColor("#FFFFFF")
        .build();

ThumbnailCandidate configured = ThumbnailCandidate.of(in, "unique_code_with_config", config);

jThumbnailer.run(candidate, new ThumbnailListener() {
     @Override
     public void onThumbnailReady(String hash, File thumbnail) {
        Files.copy(thumbnail.toPath(), Path.of("my_thumbnail_folder", thumbnail.getName()), StandardCopyOption.REPLACE_EXISTING);
     }

     @Override
     public void onThumbnailFailed(String hash, String message, int code) {
        // handle the situation
     }
});

// close thumbnailer
jThumbnailer.close();
```

## Configuration Args
| Configuration Args                        | Description                                                         |
| ----------------------------------------- | ------------------------------------------------------------------- |
| **OPENAPI Properties**                    |                                                                     |
| JTHUMBNAILER_OPENAPI_NAME                 | Application name, e.g. Java Thumbnail Generator in Swagger UI       |
| JTHUMBNAILER_OPENAPI_DESC                 | Description of the application in Swagger UI                        |
| JTHUMBNAILER_OPENAPI_LICENSE              | License of the application as displayed in Swagger UI               |
| JTHUMBNAILER_OPENAPI_URL                  | URL pointing to the license text for Swagger UI                     |
| **OPENOFFICE Properties**                 |                                                                     |
| JTHUMBNAILER_OPENOFFICE_PORTS             | Ports used by OpenOffice for document conversion                    |
| JTHUMBNAILER_OPENOFFICE_TIMEOUT           | Timeout for OpenOffice document conversion tasks                    |
| JTHUMBNAILER_OPENOFFICE_MAX_TASKS_PER_PROCESS | Maximum number of conversion tasks allowed per OpenOffice process   |
| JTHUMBNAILER_OPENOFFICE_OFFICE_HOME       | Directory path to the OpenOffice installation                       |
| JTHUMBNAILER_OPENOFFICE_WORKING_DIR       | Working directory path for OpenOffice                               |
| JTHUMBNAILER_OPENOFFICE_TMP_DIR           | Directory path for temporary files generated and used by OpenOffice |
| **THUMBNAIL Properties**                  |                                                                     |
| JTHUMBNAILER_THUMBNAIL_THUMB_WIDTH        | Width of generated thumbnails                                       |
| JTHUMBNAILER_THUMBNAIL_THUMB_HEIGHT       | Height of generated thumbnails                                      |
| **ASYNC Properties**                      |                                                                     |
| JTHUMBNAILER_ASYNC_CORE_POOL_SIZE         | Core pool size for the asynchronous processing tasks                |
| JTHUMBNAILER_ASYNC_MAX_POOL_SIZE          | Maximum pool size for the asynchronous processing tasks             |
| **SERVER Properties**                     |                                                                     |
| JTHUMBNAILER_SERVER_UPLOAD_DIRECTORY      | Directory used to store uploads when using API                      |
| JTHUMBNAILER_SERVER_MAX_WAITING_LIST_SIZE | Queue for files to be processed                                     |
| **SPRING**                                |                                                                     |
| SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE    | Maximum allowed file size for multipart file uploads                |
| SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE | Maximum allowed request size for multipart file uploads             |
| SERVER_PORT                               | Port on which the Web application will be hosted                    |
| **SPRING DOC**                            |                                                                     |
| SPRINGDOC_API_DOCS_PATH                   | Path for accessing the API documentation in JSON format             |
| SPRINGDOC_SWAGGER_UI_PATH                 | Path for accessing the Swagger UI for interactive API documentation |

- All parameters can be passed through environment variables. To pass a param as environment variable you need to replace the dots with underscore and use uppercase. For example, `jthumbnailer.openoffice.office_home` should be 
`JTHUMBNAILER_OPENOFFICE_OFFICEHOME` (see [Spring documentation](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding).

## Multi-module build

The project is split into **Maven-style Gradle modules** so you can depend only on what you need (e.g. Kafka without AMQP/gRPC/storage). Each module can be **built and published independently**.

- **Build all:** `./gradlew build`
- **Publish all:** `./gradlew publish`
- **Details:** [docs/MULTI_MODULE.md](docs/MULTI_MODULE.md)

| Module | Artifact | Purpose |
|--------|----------|---------|
| `jthumbnail-core` | `io.github.makbn:jthumbnail-core` | API, job processing, thumbnailers |
| `jthumbnail-kafka` | `io.github.makbn:jthumbnail-kafka` | Kafka job queue |
| `jthumbnail-webservice` | `io.github.makbn:jthumbnail-webservice` | REST API |
| `jthumbnail-webhook` | `io.github.makbn:jthumbnail-webhook` | Webhook connector |
| `jthumbnail-amqp` | `io.github.makbn:jthumbnail-amqp` | AMQP connector |
| `jthumbnail-grpc` | `io.github.makbn:jthumbnail-grpc` | gRPC server |
| `jthumbnail-watcher` | `io.github.makbn:jthumbnail-watcher` | Filesystem watcher |
| `jthumbnail-storage` | `io.github.makbn:jthumbnail-storage` | S3 storage connector |
| `jthumbnail-mcp` | `io.github.makbn:jthumbnail-mcp` | MCP server (LLM tools) |
| `jthumbnail-app` | `io.github.makbn:jthumbnail-app` | Spring Boot application |
| `jthumbnail-spring-boot-starter` | `io.github.makbn:jthumbnail-spring-boot-starter` | Spring Boot starter for embedding core |

## Module documentation

Each module has a **detailed README** under [docs/](docs/) with build, run, and use instructions:

| Module | Description | Documentation |
|--------|-------------|---------------|
| **Application** | Main Spring Boot app: build, run, config | [docs/application/README.md](docs/application/README.md) |
| **Connector API** | Public API for submitting jobs (connector authors) | [docs/connector-api/README.md](docs/connector-api/README.md) |
| **Core** | Job processing, OpenOffice, thumbnail size, async | [docs/core/README.md](docs/core/README.md) |
| **Webservice** | REST API: upload, jobs, status, Swagger | [docs/webservice/README.md](docs/webservice/README.md) |
| **Webhook** | HTTP webhook for CMS/CI (POST JSON → job) | [docs/webhook/README.md](docs/webhook/README.md) |
| **Kafka** | Job queue: produce/consume, DLQ, retry | [docs/kafka/README.md](docs/kafka/README.md) |
| **AMQP** | RabbitMQ: consume messages, create/process jobs | [docs/amqp/README.md](docs/amqp/README.md) |
| **gRPC** | gRPC server for submitting jobs | [docs/grpc/README.md](docs/grpc/README.md) |
| **Watcher** | Filesystem watcher: auto-submit new files | [docs/watcher/README.md](docs/watcher/README.md) |
| **Storage** | S3-compatible: webhook/SQS → thumbnail → upload | [docs/storage/README.md](docs/storage/README.md) |
| **MCP** | MCP server for LLMs (Claude Desktop, Cursor) | [docs/mcp/README.md](docs/mcp/README.md) |

Full index: [docs/README.md](docs/README.md).

## Connectors and extensibility

Thumbnail jobs can be triggered via **REST**, **Kafka**, **AMQP**, **gRPC**, **webhook (CMS)**, **filesystem watcher**, and **S3-compatible storage**. All connectors follow a single public contract so the system is extensible by third-party developers.

- **Public API:** [`io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter`](src/main/java/io/github/makbn/jthumbnail/connector/api/ThumbnailJobSubmitter.java) — use this to submit jobs from any connector.
- **Specification:** [docs/CONNECTOR_SPECIFICATION.md](docs/CONNECTOR_SPECIFICATION.md) — lifecycle, configuration pattern, and how to add new connectors.

## Requirements

- Java JRE **21**
- OpenOffice >4.x or LibreOffice >7.x

## Supported File Formats

- Office files (`doc`, `docx`, `xls`, `xlsx`, `ppt`, `pptx`)
  -  There is a problem with most xlsx files 
- OpenOffice files (all of them)
- Text files (`txt`, `pdf`, `rtf`, `html`)
- Image files (`jpg`, `png`, `bmp`, `gif`)
- AutoCad files (`dwg`)
- MP3 files (user album-art as thumbnail)
- MPEG files (generate gif file)

## Adding Repository

Packages are currently published on Github Registry.Please [read this document](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#installing-a-package) for more information on using them.

### Maven

**Step 1**. Add the dependency to the `pom` file:

```xml
<dependency>
  <groupId>io.github.makbn</groupId>
  <artifactId>jthumbnail</artifactId>
  <version>2.3.0</version>
</dependency>
```

### Gradle

**Step 1**. Add the dependency to the `build.gradle` file:

```gradle
compile "io.github.makbn:jthumbnail:2.3.0"
```

## TODO

All the tasks and features that are planned to be implemented are moved to the [Project board](https://github.com/users/makbn/projects/3)!

## Contributing
Contributions are welcome! Please read the [contributing guidelines](CONTRIBUTING.md), [Code of Conduct](CODE_OF_CONDUCT.md), and [CLA](CLA.md) before opening issues or pull requests.

## Original project

**JThumbnail** is based on an [old project](https://github.com/benjaminpick/java-thumbnailer) of the university of Siegen for the benefit of [come_IN Computerclubs](http://www.computerclub-comein.de). and thanks a lot to @benjaminpick
