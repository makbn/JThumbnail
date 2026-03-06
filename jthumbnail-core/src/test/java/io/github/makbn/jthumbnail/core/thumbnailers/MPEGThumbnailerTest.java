package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MPEGThumbnailerTest {

    @TempDir
    Path tempDir;

    private MPEGThumbnailer thumbnailer;

    @BeforeEach
    void setUp() {
        ThumbnailProperties appProps = new ThumbnailProperties(400, 535);
        thumbnailer = new MPEGThumbnailer(appProps);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "video/mp4",
            "video/MP2T",
            "video/x-msvideo",
            "video/x-ms-wmv",
            "video/x-m4v",
            "video/webm",
            "video/quicktime",
            "video/3gpp"
    })
    void shouldAcceptMimeType(String mimeType) {
        List<String> accepted = Arrays.asList(thumbnailer.getAcceptedMIMETypes());
        assertTrue(accepted.contains(mimeType), "Should accept " + mimeType);
    }

    @Test
    void getAcceptedMIMETypesReturnsNotNull() {
        assertNotNull(thumbnailer.getAcceptedMIMETypes());
    }

    @Test
    void generateThumbnailShouldThrowExceptionOnInvalidFile() {
        File nonExistent = tempDir.resolve("non_existent.mp4").toFile();
        File output = tempDir.resolve("output.gif").toFile();

        // FFmpegFrameGrabber will fail to start on a non-existent file
        assertThrows(ThumbnailException.class, () -> {
            thumbnailer.generateThumbnail(nonExistent, output);
        });
    }

    @Test
    void generateThumbnailShouldThrowExceptionOnEmptyFile() throws Exception {
        File emptyFile = tempDir.resolve("empty.mp4").toFile();
        emptyFile.createNewFile();
        File output = tempDir.resolve("output.gif").toFile();

        // FFmpegFrameGrabber will fail to start on an empty file (invalid format)
        assertThrows(ThumbnailException.class, () -> {
            thumbnailer.generateThumbnail(emptyFile, output);
        });
    }
}
