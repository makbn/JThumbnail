package io.github.makbn.jthumbnail.core;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailRuntimeException;
import io.github.makbn.jthumbnail.core.provider.ThumbnailProviderRegistry;
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeDetector;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Delegates thumbnail generation to the pluggable {@link ThumbnailProviderRegistry}.
 * Providers are selected dynamically by file type; no hardcoded provider logic here.
 */
@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThumbnailerManager {

    ThumbnailProviderRegistry registry;
    MimeTypeDetector mimeTypeDetector;

    @NonFinal
    File thumbnailFolder;

    public ThumbnailerManager(ThumbnailProviderRegistry registry, MimeTypeDetector mimeTypeDetector) {
        this.registry = registry;
        this.mimeTypeDetector = mimeTypeDetector;
    }

    public File chooseThumbnailFilename(File input, String ext) throws ThumbnailException {
        if (thumbnailFolder == null) {
            try {
                thumbnailFolder = Files.createTempDirectory("jthumbnailer").toFile();
            } catch (IOException e) {
                throw new ThumbnailException(e);
            }
        }
        if (input == null) throw new IllegalArgumentException("Input file may not be null");
        return new File(
                thumbnailFolder, String.format("%s%s.%s", FilenameUtils.getBaseName(input.getName()), "_thumb", ext));
    }

    public File createThumbnail(File input, String ext) throws ThumbnailRuntimeException, ThumbnailException {
        File output = chooseThumbnailFilename(input, ext);
        String mimeType = getMIMEType(input);
        registry.generateThumbnail(input, output, mimeType);
        return output;
    }

    private String getMIMEType(File input) throws ThumbnailException {
        try {
            String result = mimeTypeDetector.getMimeType(input);
            log.debug("Detected MIME type: {}", result);
            return result;
        } catch (IOException e) {
            throw new ThumbnailException(e);
        }
    }

    public void generateThumbnail(File input, File output, String mimeType)
            throws ThumbnailRuntimeException, ThumbnailException {
        if (!Files.exists(input.toPath())) {
            throw new ThumbnailException("the input file does not exist");
        }
        String resolved = mimeType != null ? mimeType : getMIMEType(input);
        registry.generateThumbnail(input, output, resolved);
    }

    public void generateThumbnail(File input, File output) throws ThumbnailRuntimeException, ThumbnailException {
        generateThumbnail(input, output, null);
    }

    public synchronized void close() throws IOException {
        registry.close();
    }

    public int getCurrentImageWidth() {
        throw new UnsupportedOperationException();
    }

    public int getCurrentImageHeight() {
        throw new UnsupportedOperationException();
    }

    public String[] getAcceptedMIMETypes() {
        throw new UnsupportedOperationException("getting accepted MIME types not allowed");
    }
}
