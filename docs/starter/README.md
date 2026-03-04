# Spring Boot Starter for JThumbnail Core

The **Spring Boot starter** module (`jthumbnail-spring-boot-starter`) makes it easy to embed the
JThumbnail **core** (thumbnailers, job model, metrics, etc.) into your own Spring Boot application
without using the built-in connectors.

## Overview

- Auto-configures the JThumbnail core using Spring Boot’s auto-configuration mechanism.
- Registers beans from `io.github.makbn.jthumbnail.core` (e.g. `JThumbnailer`, `ThumbnailerManager`,
  job services, metrics).
- Respects existing configuration properties (`jthumbnailer.*`) so you can tune thumbnail size,
  OpenOffice/LibreOffice, async pool, rate limit, etc.

This is useful when:

- You already have a Spring Boot 3+/4-style application and want to call the thumbnail engine
  programmatically.
- You don’t need (or want) the REST/webservice or connector modules, but you want the core
  thumbnail pipeline as a library.

## Dependency

Add the starter as a dependency in your Spring Boot application:

```gradle
dependencies {
    implementation "io.github.makbn:jthumbnail-spring-boot-starter:2.3.0"
}
```

or in Maven:

```xml
<dependency>
  <groupId>io.github.makbn</groupId>
  <artifactId>jthumbnail-spring-boot-starter</artifactId>
  <version>2.3.0</version>
</dependency>
```

This transitively pulls in `jthumbnail-core` and its dependencies.

## Auto-configuration

The starter registers a single auto-configuration:

```java
@AutoConfiguration
@ConditionalOnClass(JThumbnailer.class)
@ComponentScan(basePackages = "io.github.makbn.jthumbnail.core")
public class JThumbnailAutoConfiguration {}
```

It is discovered via:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

When on the classpath, your Spring Boot application automatically gains the core beans from
`io.github.makbn.jthumbnail.core`, including:

- `JThumbnailer`
- `ThumbnailerManager`
- Thumbnail provider registry and thumbnailers
- Job model and services
- Metrics

## Configuration

You configure the core using the same properties as the main application. Example `application.yml`:

```yaml
jthumbnailer:
  thumbnail:
    thumb-width: 400
    thumb-height: 535

  openoffice:
    manager_type: none   # or local/remote/external if LibreOffice/OpenOffice available

  async:
    core-pool-size: 10
    max-pool-size: 32

  rate-limit:
    enabled: false
```

If you use JPA/job persistence in your application, also configure `spring.datasource.*` and
`spring.jpa.*` as usual (the core uses Spring Data JPA for `ThumbnailJob`).

## Programmatic usage

In your Spring Boot application:

```java
import io.github.makbn.jthumbnail.core.JThumbnailer;
import io.github.makbn.jthumbnail.core.listener.ThumbnailListener;
import io.github.makbn.jthumbnail.core.model.ThumbnailCandidate;
import io.github.makbn.jthumbnail.core.model.ThumbnailConfig;

@Service
public class MyThumbnailService {

    private final JThumbnailer thumbnailer;

    public MyThumbnailService(JThumbnailer thumbnailer) {
        this.thumbnailer = thumbnailer;
    }

    public void generateThumbnail(File input, String uid) {
        ThumbnailConfig config = ThumbnailConfig.defaultConfig();
        ThumbnailCandidate candidate = ThumbnailCandidate.of(input, uid, config);

        thumbnailer.run(candidate, new ThumbnailListener() {
            @Override
            public void onThumbnailReady(String hash, File thumbnail) {
                // handle thumbnail
            }

            @Override
            public void onThumbnailFailed(String hash, String message, int code) {
                // handle failure
            }
        });
    }
}
```

Because this runs inside your own Spring Boot application, you can integrate tightly with your own
domain model, persistence, and messaging.

## Notes

- The starter does **not** expose REST, GraphQL, webhook, or other connectors. Those remain
  available as separate modules if you need them.
- You are responsible for configuring:
  - Database/JPA (if you want persistent jobs).
  - Office/LibreOffice (if you need document conversion).
  - Any external dependencies (e.g. FFmpeg in your runtime environment).

For details on the underlying core capabilities and configuration keys, see
[`docs/core/README.md`](../core/README.md).

