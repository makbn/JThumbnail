package io.github.makbn.jthumbnail.core.util;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipFile;

@Log4j2
public class IOUtil {

    private IOUtil(){
        // do nothing
    }

    public static void quietlyClose(ZipFile zipFile) {
        try {
            if (zipFile != null)
                zipFile.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    public static void deleteQuietlyForce(File file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                log.debug(e);
                // ignored
            }
        }
    }
}
