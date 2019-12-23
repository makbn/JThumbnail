/*
 * regain/Thumbnailer - A file search engine providing plenty of formats (Plugin)
 * Copyright (C) 2011  Come_IN Computerclubs (University of Siegen)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Come_IN-Team <come_in-team@listserv.uni-siegen.de>
 */

package io.github.makbn.thumbnailer.util.mime;


import io.github.makbn.thumbnailer.util.IOUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Add detection of Office2007 files (and OpenOffice files).
 * Magic numbers don't help here, only introspection of the zip.
 */
public class Office2007FileIdentifier implements MimeTypeIdentifier {

    private static Logger mLog = LogManager.getLogger("Office2007FileIdentifier");

    @Override
    public String identify(String mimeType, byte[] bytes, File file) {
        if (mimeType == null || mimeType.equals("application/zip") || mimeType.startsWith("application/vnd.")) {
            ZipFile zipFile = null;
            ZipEntry entry = null;
            try {
                zipFile = new ZipFile(file);

                entry = zipFile.getEntry("word/document.xml");
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
            } finally {
                IOUtil.quietlyClose(zipFile);
            }
        }

        return mimeType;

    }

    private String detectOpenOfficeMimeType(InputStream inputStream) throws IOException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            return in.readLine();
        } finally {
            IOUtil.quietlyClose(inputStream);
        }
    }

    @Override
    public List<String> getExtensionsFor(String mimeType) {
        return new ArrayList<String>() {{
            add("docx");
        }};
    }

    @Override
    public String getThumbnailExtension() {
        return "png";
    }
}
