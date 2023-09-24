package io.github.makbn.jthumbnail.core.thumbnailers;


import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerRuntimeException;
import io.github.makbn.jthumbnail.core.util.IOUtil;
import io.github.makbn.jthumbnail.core.util.ResizeImage;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
public class OpenOfficeThumbnailer extends AbstractThumbnailer {

    private static final Logger logger = LogManager.getLogger(OpenOfficeThumbnailer.class);
    private final PDFBoxThumbnailer pdfBoxThumbnailer;

    @Autowired
    public OpenOfficeThumbnailer(AppSettings appSettings, PDFBoxThumbnailer pdfBoxThumbnailer) {
        super(appSettings);
        this.pdfBoxThumbnailer = pdfBoxThumbnailer;
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        if (FilenameUtils.getExtension(input.getName()).equalsIgnoreCase("pdf")) {
            pdfBoxThumbnailer.generateThumbnail(input, output);
        } else {
            ZipFile zipFile;

            try {
                zipFile = new ZipFile(input);
            } catch (ZipException e) {
                logger.warn("OpenOfficeThumbnailer", e);
                throw new ThumbnailerException("This is not a zipped file. Is this really an OpenOffice file?", e);
            } catch (IOException e) {
                throw new ThumbnailerException(e);
            }
            ZipEntry entry = zipFile.getEntry("Thumbnails/thumbnail.png");

            try(BufferedInputStream in = new BufferedInputStream(zipFile.getInputStream(entry))) {
                ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);
                resizer.setInputImage(in);
                resizer.writeOutput(output);
            } catch (RuntimeException re) {
                throw new ThumbnailerRuntimeException(re);
            } catch (Exception e) {
                throw new ThumbnailerException(e.getMessage());
            } finally {
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
    @Override
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
