package io.github.makbn.jthumbnail.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

class FileTypeTest {

    @TempDir
    Path tempDir;

    @Test
    void ofMimeOnly() {
        FileType ft = FileType.of("image/png");
        assertEquals("image/png", ft.mimeType());
        assertNull(ft.extension());
    }

    @Test
    void ofMimeAndExtension() {
        FileType ft = FileType.of("video/mp4", "mp4");
        assertEquals("video/mp4", ft.mimeType());
        assertEquals("mp4", ft.extension());
    }

    @Test
    void fromFileDerivesExtension() throws Exception {
        File f = tempDir.resolve("doc.pdf").toFile();
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        f.createNewFile();
        FileType ft = FileType.fromFile(f, "application/pdf");
        assertEquals("application/pdf", ft.mimeType());
        assertEquals("pdf", ft.extension());
    }

    @Test
    void fromFileNullFile() {
        FileType ft = FileType.fromFile(null, "text/plain");
        assertEquals("text/plain", ft.mimeType());
        assertNull(ft.extension());
    }

    @Test
    void fromFileNoExtension() throws Exception {
        File f = tempDir.resolve("noext").toFile();
        f.createNewFile();
        FileType ft = FileType.fromFile(f, "application/octet-stream");
        assertEquals("application/octet-stream", ft.mimeType());
        assertNull(ft.extension());
    }
}
