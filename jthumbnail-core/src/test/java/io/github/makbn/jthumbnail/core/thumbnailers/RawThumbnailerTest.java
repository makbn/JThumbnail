package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RawThumbnailerTest {
    private ThumbnailProperties props;

    @BeforeEach
    void setUp() {
        props = Mockito.mock(ThumbnailProperties.class);
        Mockito.when(props.thumbWidth()).thenReturn(100);
        Mockito.when(props.thumbHeight()).thenReturn(100);
    }

    @Test
    void testRawThumbnailerMime() throws IOException {
        try (RawThumbnailer t = new RawThumbnailer(props)) {
            assertTrue(Arrays.asList(t.getAcceptedMIMETypes()).contains("image/x-adobe-dng"));
        }
    }
}
