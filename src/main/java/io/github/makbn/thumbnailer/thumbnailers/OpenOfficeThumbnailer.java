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

package io.github.makbn.thumbnailer.thumbnailers;


import io.github.makbn.thumbnailer.exception.ThumbnailerException;
import io.github.makbn.thumbnailer.exception.ThumbnailerRuntimeException;
import io.github.makbn.thumbnailer.util.IOUtil;
import io.github.makbn.thumbnailer.util.ResizeImage;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * This class extracts Thumbnails from OpenOffice-Files.
 * <p>
 * Depends:
 * <li> <i>NOT</i> on OpenOffice, as the Thumbnail is already inside the file. (184x256px regardless of page orientation)
 * (So if the thumbnail generation is not correct, it's OpenOffice's fault, not our's :-)
 */
public class OpenOfficeThumbnailer extends AbstractThumbnailer {

    private static final Logger logger = LogManager.getLogger(OpenOfficeThumbnailer.class);
    private static final PDFBoxThumbnailer pdfBoxThumbnailer = new PDFBoxThumbnailer();

    @Override
    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {
        if (FilenameUtils.getExtension(input.getName()).equalsIgnoreCase("pdf")) {
            pdfBoxThumbnailer.generateThumbnail(input, output);
        } else {
            BufferedInputStream in = null;
            ZipFile zipFile;

            try {
                zipFile = new ZipFile(input);
            } catch (ZipException e) {
                logger.warn("OpenOfficeThumbnailer", e);
                throw new ThumbnailerException("This is not a zipped file. Is this really an OpenOffice file?", e);
            }

            try {
                ZipEntry entry = zipFile.getEntry("Thumbnails/thumbnail.png");
                if (entry == null)
                    throw new ThumbnailerException("Zip file does not contain 'Thumbnails/thumbnail.png' . Is this really an OpenOffice file?");

                in = new BufferedInputStream(zipFile.getInputStream(entry));

                ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);
                resizer.setInputImage(in);
                resizer.writeOutput(output);

                in.close();
            } catch (RuntimeException re) {
                throw new ThumbnailerRuntimeException(re);
            } catch (Exception e) {
                throw new ThumbnailerException(e.getMessage());
            } finally {
                IOUtil.quietlyClose(in);
                IOUtil.quietlyClose(zipFile);
            }
        }
    }

    /**
     * Get a List of accepted File Types.
     * All OpenOffice Formats are accepted.
     *
     * @return MIME-Types
     */
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "application/vnd.sun.xml.writer",
                "application/vnd.sun.xml.writer.template",
                "application/vnd.sun.xml.writer.global",
                "application/vnd.sun.xml.calc",
                "application/vnd.sun.xml.calc.template",
                "application/vnd.stardivision.calc",
                "application/vnd.sun.xml.impress",
                "application/vnd.sun.xml.impress.template ",
                "application/vnd.stardivision.impress sdd",
                "application/vnd.sun.xml.draw",
                "application/vnd.sun.xml.draw.template",
                "application/vnd.stardivision.draw",
                "application/vnd.sun.xml.math",
                "application/vnd.stardivision.math",
                "application/vnd.oasis.opendocument.text",
                "application/vnd.oasis.opendocument.text-template",
                "application/vnd.oasis.opendocument.text-web",
                "application/vnd.oasis.opendocument.text-master",
                "application/vnd.oasis.opendocument.graphics",
                "application/vnd.oasis.opendocument.graphics-template",
                "application/vnd.oasis.opendocument.presentation",
                "application/vnd.oasis.opendocument.presentation-template",
                "application/vnd.oasis.opendocument.spreadsheet",
                "application/vnd.oasis.opendocument.spreadsheet-template",
                "application/vnd.oasis.opendocument.chart",
                "application/vnd.oasis.opendocument.formula",
                "application/vnd.oasis.opendocument.database",
                "application/vnd.oasis.opendocument.image",
                "text/html",
                "application/zip" /* Could be an OpenOffice file! */
        };
    }

}
