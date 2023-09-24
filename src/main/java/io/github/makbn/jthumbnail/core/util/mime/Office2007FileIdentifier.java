package io.github.makbn.jthumbnail.core.util.mime;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Add detection of Office2007 files (and OpenOffice files).
 * Magic numbers don't help here, only introspection of the zip.
 */
public class Office2007FileIdentifier implements MimeTypeIdentifier {

    private static final Logger mLog = LogManager.getLogger("Office2007FileIdentifier");

    @Override
    public String identify(String mimeType, byte[] bytes, File file) {
        if (mimeType == null || mimeType.equals("application/zip") || mimeType.startsWith("application/vnd.")) {
            try(ZipFile zipFile = new ZipFile(file)) {
                ZipEntry entry = zipFile.getEntry("word/document.xml");

                if (entry != null)
                    return "application/vnd.openxmlformats-officedocument.wordprocessingml";

                entry = zipFile.getEntry("ppt/presentation.xml");
                if (entry != null)
                    return "application/vnd.openxmlformats-officedocument.presentationml";

                entry = zipFile.getEntry("xl/workbook.xml");
                if (entry != null)
                    return "application/vnd.openxmlformats-officedocument.spreadsheetml";

                entry = zipFile.getEntry("mimetype");
                if (entry != null)
                    return detectOpenOfficeMimeType(zipFile.getInputStream(entry));
            } catch (ZipException e) {
                return mimeType; // Zip file damaged or whatever. Silently give up.
            } catch (IOException e) {
                mLog.error(e);
                return mimeType; // Zip file damaged or whatever. Silently give up.
            }
        }

        return mimeType;

    }

    private String detectOpenOfficeMimeType(InputStream inputStream) throws IOException {
        try( BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            return in.readLine();
        }
    }

    @Override
    public List<String> getExtensionsFor(String mimeType) {
        return List.of("docx");
    }

    @Override
    public String getThumbnailExtension() {
        return "png";
    }
}
