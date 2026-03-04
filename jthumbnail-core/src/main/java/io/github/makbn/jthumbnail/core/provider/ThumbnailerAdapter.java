package io.github.makbn.jthumbnail.core.provider;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.thumbnailers.Thumbnailer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wraps a {@link Thumbnailer} as a {@link ThumbnailProvider} for use in the registry.
 * Support is derived from {@link Thumbnailer#getAcceptedMIMETypes()}.
 */
public class ThumbnailerAdapter implements ThumbnailProvider, Closeable {

    private final Thumbnailer thumbnailer;

    public ThumbnailerAdapter(Thumbnailer thumbnailer) {
        this.thumbnailer = thumbnailer;
    }

    @Override
    public boolean supports(FileType fileType) {
        if (fileType == null || fileType.mimeType() == null) return false;
        String[] accepted = thumbnailer.getAcceptedMIMETypes();
        if (accepted == null) return true; // Thumbnailer accepts all
        Set<String> set = Arrays.stream(accepted).collect(Collectors.toSet());
        return set.contains(fileType.mimeType());
    }

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailException {
        thumbnailer.generateThumbnail(input, output, mimeType);
    }

    @Override
    public String getName() {
        return thumbnailer.getClass().getSimpleName();
    }

    @Override
    public void close() throws IOException {
        thumbnailer.close();
    }
}
