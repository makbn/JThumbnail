package io.github.makbn.jthumbnail.core.util.mime;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Add detection of Office2007 files (and OpenOffice files).
 * Magic numbers don't help here, only introspection of the zip.
 */
@Slf4j
public class Office2007FileIdentifier implements MimeTypeIdentifier {

    @Override
    public String identify(String mimeType, byte[] bytes, File file) {
        if (mimeType == null || mimeType.equals("application/zip") || mimeType.startsWith("application/vnd.")) {
            try (ZipFile zipFile = new ZipFile(file)) {
                ZipEntry entry = zipFile.getEntry("word/document.xml");

                if (entry != null) return "application/vnd.openxmlformats-officedocument.wordprocessingml";

                entry = zipFile.getEntry("ppt/presentation.xml");
                if (entry != null) return "application/vnd.openxmlformats-officedocument.presentationml";

                entry = zipFile.getEntry("xl/workbook.xml");
                if (entry != null) return "application/vnd.openxmlformats-officedocument.spreadsheetml";

                entry = zipFile.getEntry("mimetype");
                if (entry != null) return detectOpenOfficeMimeType(zipFile.getInputStream(entry));
            } catch (ZipException e) {
                return mimeType; // Zip file damaged or whatever. Silently give up.
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return mimeType; // Zip file damaged or whatever. Silently give up.
            }
        }

        return mimeType;
    }

    private String detectOpenOfficeMimeType(InputStream inputStream) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
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
