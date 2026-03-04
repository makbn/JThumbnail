package io.github.makbn.jthumbnail.core.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.thumbnailers.Thumbnailer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class ThumbnailerAdapterTest {

    private Thumbnailer thumbnailer;
    private ThumbnailerAdapter adapter;

    @BeforeEach
    void setUp() {
        thumbnailer = mock(Thumbnailer.class);
        adapter = new ThumbnailerAdapter(thumbnailer);
    }

    /** Real implementation so getName() returns a predictable simple name. */
    private static class StubThumbnailer implements Thumbnailer {
        @Override
        public void generateThumbnail(File input, File output, String mimeType) {}

        @Override
        public void generateThumbnail(File input, File output) {}

        @Override
        public void close() {}

        @Override
        public int getCurrentImageWidth() {
            return 0;
        }

        @Override
        public int getCurrentImageHeight() {
            return 0;
        }

        @Override
        public String[] getAcceptedMIMETypes() {
            return new String[] {"image/png"};
        }
    }

    @Test
    void supportsWhenMimeInAcceptedList() {
        when(thumbnailer.getAcceptedMIMETypes()).thenReturn(new String[] {"image/png", "image/jpeg"});
        assertTrue(adapter.supports(FileType.of("image/png")));
        assertTrue(adapter.supports(FileType.of("image/jpeg")));
        assertFalse(adapter.supports(FileType.of("video/mp4")));
    }

    @Test
    void supportsWhenAcceptedNullTreatsAsAcceptAll() {
        when(thumbnailer.getAcceptedMIMETypes()).thenReturn(null);
        assertTrue(adapter.supports(FileType.of("application/octet-stream")));
    }

    @Test
    void supportsReturnsFalseForNullFileType() {
        assertFalse(adapter.supports(null));
    }

    @Test
    void supportsReturnsFalseForNullMimeType() {
        assertFalse(adapter.supports(FileType.of(null)));
    }

    @Test
    void getNameReturnsThumbnailerClassSimpleName() {
        ThumbnailerAdapter adapterWithStub = new ThumbnailerAdapter(new StubThumbnailer());
        assertEquals("StubThumbnailer", adapterWithStub.getName());
    }

    @Test
    void generateThumbnailDelegatesToThumbnailer() throws IOException, ThumbnailException {
        File in = new File("/tmp/in.png");
        File out = new File("/tmp/out.png");
        adapter.generateThumbnail(in, out, "image/png");
        verify(thumbnailer).generateThumbnail(in, out, "image/png");
    }

    @Test
    void closeDelegatesToThumbnailer() throws IOException {
        adapter.close();
        verify(thumbnailer).close();
    }
}
