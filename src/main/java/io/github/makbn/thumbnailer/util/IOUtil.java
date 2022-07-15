package io.github.makbn.thumbnailer.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class IOUtil {

    private static final Logger mLog = LogManager.getLogger("IOUtil");

    /**
     * Close, ignoring IOExceptions
     *
     * @param stream Stream to be closed. May be null (in this case, nothing is done).
     */
    public static void quietlyClose(Closeable stream) {
        try {
            if (stream != null)
                stream.close();
        } catch (IOException e) {
            mLog.error(e);
        }
    }

    public static void quietlyClose(ZipFile zipFile) {
        try {
            if (zipFile != null)
                zipFile.close();
        } catch (IOException e) {
            mLog.error(e);
        }
    }

    public static void deleteQuietlyForce(File file) {
        if (file != null && !file.delete() && file.exists()) {
            file.deleteOnExit();
        }
    }

    /**
     * Simplistic version: return the substring after the base
     */
    public static String getRelativeFilename(File base, File target) {
        return getRelativeFilename(base.getAbsolutePath(), target.getAbsolutePath());
    }

    public static String getRelativeFilename(String sBase, String sTarget) {
        if (sTarget.startsWith(sBase)) {
            if (sBase.endsWith("/") || sBase.endsWith("\\") || sTarget.length() == sBase.length())
                return sTarget.substring(sBase.length());
            else
                return sTarget.substring(sBase.length() + 1);
        } else
            return sTarget; // Leave absolute
    }
}
