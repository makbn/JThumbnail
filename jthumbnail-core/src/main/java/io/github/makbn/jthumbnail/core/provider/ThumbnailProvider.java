package io.github.makbn.jthumbnail.core.provider;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;

import java.io.File;
import java.io.IOException;

/**
 * Pluggable thumbnail generation. Implement and register as a Spring bean or via
 * {@link ThumbnailProviderRegistry#register(ThumbnailProvider)}; selection is by file type.
 */
public interface ThumbnailProvider {

    /** True if this provider can generate a thumbnail for the given file type. */
    boolean supports(FileType fileType);

    /**
     * Generate thumbnail from input to output.
     *
     * @throws IOException       if file I/O fails
     * @throws ThumbnailException if generation fails
     */
    void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailException;

    /** Name used for priority order and logging (default: class simple name). */
    default String getName() {
        return getClass().getSimpleName();
    }
}
