package io.github.makbn.jthumbnail.core.util.mime;

import java.io.File;
import java.util.List;

/**
 * Helper Classes for MimeTypeDetector.
 *
 * @author Benjamin
 */
public interface MimeTypeIdentifier {

    /**
     * Try to identify the mimeType.
     * <p>
     * Contract: If the implementing class doesn't know anything,
     * it returns the current mimeType.
     *
     * @param mimeType Currently detected mimeType
     * @param bytes    512 Bytes of Header for Magic Detection
     * @param file     Filename of the File to detect
     * @return MIME Type detected.
     */
    String identify(String mimeType, byte[] bytes, File file);

    /**
     * Get File Extensions for a known MIME Type.
     *
     * @param mimeType
     * @return List of file extensions (main extension first).
     */
    List<String> getExtensionsFor(String mimeType);

    String getThumbnailExtension();
}
