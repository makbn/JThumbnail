package io.github.makbn.jthumbnail.core.provider;

import java.io.File;

/**
 * File type for provider selection: MIME type and optional file extension.
 */
public record FileType(String mimeType, String extension) {

    /** From MIME type only. */
    public static FileType of(String mimeType) {
        return new FileType(mimeType, null);
    }

    /** From MIME type and extension. */
    public static FileType of(String mimeType, String extension) {
        return new FileType(mimeType, extension);
    }

    /** From file path and detected MIME (extension derived from filename). */
    public static FileType fromFile(File file, String detectedMimeType) {
        String ext = null;
        if (file != null && file.getName() != null) {
            int dot = file.getName().lastIndexOf('.');
            if (dot > 0) {
                ext = file.getName().substring(dot + 1);
            }
        }
        return new FileType(detectedMimeType, ext);
    }
}
