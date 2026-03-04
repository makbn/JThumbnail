package io.github.makbn.jthumbnail.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.provider.ThumbnailProviderRegistry;
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeDetector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

class ThumbnailerManagerTest {

    @TempDir
    Path tempDir;

    private ThumbnailProviderRegistry registry;
    private ThumbnailerManager manager;

    @BeforeEach
    void setUp() {
        registry = mock(ThumbnailProviderRegistry.class);
        manager = new ThumbnailerManager(registry, new MimeTypeDetector());
    }

    @Test
    void chooseThumbnailFilenameCreatesFileInTempDir() throws ThumbnailException {
        File input = tempDir.resolve("document.pdf").toFile();
        input.getParentFile().mkdirs();
        try {
            input.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File out = manager.chooseThumbnailFilename(input, "png");
        assertNotNull(out);
        assertEquals("document_thumb.png", out.getName());
        assertNotNull(out.getParentFile());
    }

    @Test
    void chooseThumbnailFilenameThrowsWhenInputNull() {
        assertThrows(IllegalArgumentException.class, () -> manager.chooseThumbnailFilename(null, "png"));
    }

    @Test
    void generateThumbnailDelegatesToRegistry() throws Exception {
        File input = tempDir.resolve("in.png").toFile();
        File output = tempDir.resolve("out.png").toFile();
        input.getParentFile().mkdirs();
        input.createNewFile();
        manager.generateThumbnail(input, output, "image/png");
        verify(registry).generateThumbnail(eq(input), eq(output), eq("image/png"));
    }

    @Test
    void generateThumbnailWithNullMimeResolvesViaDetector() throws Exception {
        File input = tempDir.resolve("in.png").toFile();
        File output = tempDir.resolve("out.png").toFile();
        input.getParentFile().mkdirs();
        input.createNewFile();
        manager.generateThumbnail(input, output, null);
        verify(registry).generateThumbnail(eq(input), eq(output), any(String.class));
    }

    @Test
    void closeDelegatesToRegistry() throws IOException {
        manager.close();
        verify(registry).close();
    }
}
