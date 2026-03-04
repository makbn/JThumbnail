package io.github.makbn.jthumbnail.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.makbn.jthumbnail.core.util.mime.MimeTypeDetector;
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeIdentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Tests for {@link MimeTypeDetectorConfiguration}: custom {@link MimeTypeIdentifier} beans
 * are added to the shared {@link MimeTypeDetector} and used for identification and output extension.
 */
class MimeTypeDetectorConfigurationTest {

    private static final String CUSTOM_MIME = "application/x-custom-test";

    @TempDir
    Path tempDir;

    private MimeTypeDetectorConfiguration config;
    private MimeTypeIdentifier customIdentifier;

    @BeforeEach
    void setUp() {
        config = new MimeTypeDetectorConfiguration();
        customIdentifier = new StubMimeTypeIdentifier();
    }

    @Test
    void mimeTypeDetectorBeanWithNoCustomIdentifiersReturnsDetector() {
        MimeTypeDetector detector = config.mimeTypeDetector(Optional.empty());
        assertNotNull(detector);
    }

    @Test
    void mimeTypeDetectorBeanWithCustomIdentifierUsesItForIdentify() throws Exception {
        MimeTypeDetector detector = config.mimeTypeDetector(Optional.of(List.of(customIdentifier)));
        File xyzFile = tempDir.resolve("sample.xyz").toFile();
        Files.createFile(xyzFile.toPath());

        String identified = detector.getMimeType(xyzFile);
        assertEquals(CUSTOM_MIME, identified);
    }

    @Test
    void mimeTypeDetectorBeanWithCustomIdentifierReturnsCorrectOutputExtension() throws Exception {
        MimeTypeDetector detector = config.mimeTypeDetector(Optional.of(List.of(customIdentifier)));
        File xyzFile = tempDir.resolve("sample.xyz").toFile();
        Files.createFile(xyzFile.toPath());

        String ext = detector.getOutputExt(xyzFile);
        assertEquals("png", ext);
    }

    @Test
    void mimeTypeDetectorBeanWithEmptyListReturnsDetector() {
        MimeTypeDetector detector = config.mimeTypeDetector(Optional.of(List.of()));
        assertNotNull(detector);
    }

    private static class StubMimeTypeIdentifier implements MimeTypeIdentifier {

        @Override
        public String identify(String mimeType, byte[] bytes, File file) {
            if (file != null
                    && file.getName() != null
                    && file.getName().toLowerCase().endsWith(".xyz")) {
                return CUSTOM_MIME;
            }
            return mimeType;
        }

        @Override
        public List<String> getExtensionsFor(String mimeType) {
            if (CUSTOM_MIME.equals(mimeType)) {
                return List.of("xyz");
            }
            return null;
        }

        @Override
        public String getThumbnailExtension() {
            return "png";
        }
    }
}
