# Extending JThumbnail: Custom Thumbnail Generators and File Types

Third-party developers can add **custom thumbnail generators** for new file types and **custom MIME type detection** so that JThumbnail recognizes and processes those files through the same pipeline as built-in formats.

## Overview

- **Thumbnail generation** is pluggable via the **ThumbnailProvider** interface (recommended) or the **Thumbnailer** interface. The core **ThumbnailProviderRegistry** discovers all such beans and tries them in order when generating a thumbnail.
- **MIME type detection** is pluggable via the **MimeTypeIdentifier** interface. You can register custom identifiers so that your file extension (e.g. `.xyz`) is correctly detected and passed to your provider.

You can extend JThumbnail when:

- Using the **main application** (`jthumbnail-app`) or any module that uses `jthumbnail-core`.
- Using the **Spring Boot starter** (`jthumbnail-spring-boot-starter`) and embedding the core in your own app.

In both cases, register your implementations as **Spring beans** (e.g. `@Component` or `@Bean`). The registry and detector pick them up automatically.

---

## 1. Custom thumbnail generator (ThumbnailProvider)

The recommended way to support a new file type is to implement **ThumbnailProvider** and expose it as a Spring bean.

### Interface

```java
package io.github.makbn.jthumbnail.core.provider;

public interface ThumbnailProvider {

    /** True if this provider can generate a thumbnail for the given file type. */
    boolean supports(FileType fileType);

    /** Generate thumbnail from input to output. */
    void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailException;

    /** Name used for priority order and logging (default: class simple name). */
    default String getName() { return getClass().getSimpleName(); }
}
```

- **FileType** is a record with `mimeType` and optional `extension`; use `FileType.of(mimeType)` or `FileType.of(mimeType, extension)`.
- The registry calls `supports(FileType)` for each file; the first provider that returns `true` and then succeeds at `generateThumbnail` is used. If it throws, the next provider is tried.

### Example: custom provider as a bean

```java
import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.provider.FileType;
import io.github.makbn.jthumbnail.core.provider.ThumbnailProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class MyCustomThumbnailProvider implements ThumbnailProvider {

    private static final String MY_MIME = "application/x-myformat";

    @Override
    public boolean supports(FileType fileType) {
        return fileType != null && MY_MIME.equals(fileType.mimeType());
    }

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailException {
        // Your logic: read input, write image to output (e.g. PNG/JPEG).
    }

    @Override
    public String getName() {
        return "MyCustomThumbnailProvider";
    }
}
```

- Put this class in a package that is **component-scanned** by your Spring Boot application (e.g. next to your `@SpringBootApplication` or under `io.github.makbn.jthumbnail` if you use the starter’s scan).
- The **ThumbnailProviderRegistry** injects `List<ThumbnailProvider>` and includes your bean in the set of providers. No extra registration step is required.

### Alternative: Thumbnailer interface

You can instead implement **Thumbnailer** (or extend **AbstractThumbnailer**) and register it as a `@Component`. The core wraps every **Thumbnailer** bean in a **ThumbnailerAdapter** and adds it to the registry.

- **Thumbnailer** has more methods: `getAcceptedMIMETypes()`, `getCurrentImageWidth()`, `getCurrentImageHeight()`, `close()`. Use this if you want to align with the existing thumbnailer implementations (e.g. image dimensions, MIME list).
- **ThumbnailProvider** is simpler and sufficient for most custom formats.

### Provider order (priority)

You can control the order in which providers are tried via configuration:

```yaml
jthumbnailer:
  providers:
    priority:
      - MyCustomThumbnailProvider   # try first for supported types
      - FfmpegThumbnailer
      - ImageThumbnailer
```

Provider names are the `getName()` values. Those listed first are tried first; unlisted providers are tried after them.

### Runtime registration

For dynamic (non-Spring) registration you can inject **ThumbnailProviderRegistry** and call:

```java
registry.register(myProvider);
// later, if needed:
registry.unregister(myProvider);
```

---

## 2. Custom MIME type detection (MimeTypeIdentifier)

If your file type is not correctly detected by the default logic (e.g. Tika or `Files.probeContentType`), you can register a **MimeTypeIdentifier** so that your extension maps to the right MIME type and thumbnail extension.

### Interface

```java
package io.github.makbn.jthumbnail.core.util.mime;

public interface MimeTypeIdentifier {

    /** Identify or override MIME type. Return current mimeType if you don't know. */
    String identify(String mimeType, byte[] bytes, File file);

    /** File extensions for this MIME type (main extension first). */
    List<String> getExtensionsFor(String mimeType);

    /** Output thumbnail extension (e.g. "png", "jpg"). */
    String getThumbnailExtension();
}
```

### Example: custom identifier as a bean

```java
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeIdentifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
public class MyFormatMimeTypeIdentifier implements MimeTypeIdentifier {

    private static final String MY_MIME = "application/x-myformat";

    @Override
    public String identify(String mimeType, byte[] bytes, File file) {
        String name = file != null ? file.getName().toLowerCase() : "";
        if (name.endsWith(".xyz")) {
            return MY_MIME;
        }
        return mimeType;
    }

    @Override
    public List<String> getExtensionsFor(String mimeType) {
        if (MY_MIME.equals(mimeType)) {
            return List.of("xyz");
        }
        return null;
    }

    @Override
    public String getThumbnailExtension() {
        return "png";
    }
}
```

- Expose it as a **Spring bean** (e.g. `@Component`). The core creates a single **MimeTypeDetector** bean and injects all **MimeTypeIdentifier** beans into it, so your identifier is used together with the built-in ones.
- Your **ThumbnailProvider** should then `supports(FileType.of(MY_MIME))` so that when a file is detected as `application/x-myformat`, your provider is selected.

---

## 3. Summary

| Goal | Interface | Registration |
|------|-----------|--------------|
| Support a new file type for thumbnailing | **ThumbnailProvider** (or **Thumbnailer**) | Implement and expose as a Spring bean (`@Component` or `@Bean`). |
| Correct MIME / extension for that type | **MimeTypeIdentifier** | Implement and expose as a Spring bean. |
| Control which provider is tried first | — | Set `jthumbnailer.providers.priority` to a list of provider names. |

- **Connectors** (REST, Kafka, webhook, etc.) do not need to change; they submit jobs by file path. The core pipeline uses the registry and detector for every job.
- For the **Spring Boot starter**, place your provider and identifier classes in a package that is component-scanned (e.g. your own package or include `io.github.makbn.jthumbnail` in the scan). The starter’s auto-configuration enables the core package scan; your beans must be in a package that your application scans.

For more on the core and configuration, see [docs/core/README.md](core/README.md) and [docs/starter/README.md](starter/README.md).
