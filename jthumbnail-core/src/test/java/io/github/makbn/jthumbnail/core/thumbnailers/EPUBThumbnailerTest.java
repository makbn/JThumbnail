package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class EPUBThumbnailerTest extends BaseThumbnailerTest {
    private ThumbnailProperties props;

    @BeforeEach
    void setUp() {
        props = Mockito.mock(ThumbnailProperties.class);
        Mockito.when(props.thumbWidth()).thenReturn(100);
        Mockito.when(props.thumbHeight()).thenReturn(100);
    }


    @Test
    void testEPUBThumbnailerMime() throws IOException {
        try (EPUBThumbnailer thumbnailer = new EPUBThumbnailer(props)) {
            assertTrue(Arrays.asList(thumbnailer.getAcceptedMIMETypes()).contains("application/epub+zip"));
        }
    }

    @Test
    void testEpubThumbnailer_generateThumbnail() throws IOException {
        try (EPUBThumbnailer thumbnailer = new EPUBThumbnailer(props)) {
            File input = new File("src/test/resources/epub_sample_1.epub");
            File output = File.createTempFile("epub_test_output", ".jpeg");
            output.deleteOnExit();
            // act
            thumbnailer.generateThumbnail(input, output);
            assertTrue(output.length() > 0);
            assertOutputIsJpeg(output);
        } catch (ThumbnailException e) {
            fail(e);
        }
    }
}
