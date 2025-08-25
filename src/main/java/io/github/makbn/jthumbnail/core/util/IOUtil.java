package io.github.makbn.jthumbnail.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipFile;

/**
 * Utility class for handling IO operations, such as closing resources or deleting files.
 * This class provides methods to handle exceptions quietly without throwing them to the caller.
 * It is designed for situations where the application can continue even if IO operations fail.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtil {

    /**
     * Quietly closes the given {@link ZipFile}, suppressing any {@link IOException} that may occur.
     * If an exception is thrown during the close operation, it is logged but not propagated.
     *
     * @param zipFile the {@link ZipFile} to be closed; can be null, in which case nothing happens.
     */
    public static void quietlyClose(ZipFile zipFile) {
        try {
            if (zipFile != null) zipFile.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Quietly deletes the given {@link File}, forcing the deletion even if it does not exist.
     * If an {@link IOException} occurs during the deletion process, it is logged but not propagated.
     *
     * @param file the {@link File} to be deleted; can be null, in which case nothing happens.
     */
    public static void deleteQuietlyForce(File file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
