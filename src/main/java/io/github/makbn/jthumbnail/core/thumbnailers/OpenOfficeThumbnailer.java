package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerRuntimeException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.util.IOUtil;
import io.github.makbn.jthumbnail.core.util.ResizeImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

/**
 * The OpenOfficeThumbnailer class is responsible for generating thumbnails for OpenOffice documents.
 * It extends the AbstractThumbnailer class and can also handle PDF files by delegating to PDFBoxThumbnailer.
 * If the file is not a PDF, it attempts to extract the thumbnail from an OpenOffice document, which is a zipped format.
 */
@Slf4j
@Component
public class OpenOfficeThumbnailer extends AbstractThumbnailer {
    private final PDFBoxThumbnailer pdfBoxThumbnailer;

    /**
     * Constructor that initializes the OpenOfficeThumbnailer with the necessary app settings and PDFBoxThumbnailer.
     *
     * @param appSettings       Application settings used by the thumbnailer.
     * @param pdfBoxThumbnailer An instance of PDFBoxThumbnailer for handling PDF files.
     */
    public OpenOfficeThumbnailer(ThumbnailProperties appProperties, PDFBoxThumbnailer pdfBoxThumbnailer) {
        super(appProperties);
        this.pdfBoxThumbnailer = pdfBoxThumbnailer;
    }

    /**
     * Generates a thumbnail for the provided input file and saves it to the specified output location.
     * If the input file is a PDF, the method delegates thumbnail generation to PDFBoxThumbnailer.
     * For OpenOffice files, it attempts to extract an embedded thumbnail image.
     *
     * @param input  The input file for which the thumbnail is to be generated.
     * @param output The output file where the generated thumbnail will be saved.
     * @throws ThumbnailerException If there are issues processing the file or extracting the thumbnail.
     */
    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        if (FilenameUtils.getExtension(input.getName()).equalsIgnoreCase("pdf")) {
            pdfBoxThumbnailer.generateThumbnail(input, output);
        } else {
            ZipFile zipFile;

            try {
                zipFile = new ZipFile(input);
            } catch (ZipException e) {
                log.warn("OpenOfficeThumbnailer", e);
                throw new ThumbnailerException("This is not a zipped file. Is this really an OpenOffice file?", e);
            } catch (IOException e) {
                throw new ThumbnailerException(e);
            }
            ZipEntry entry = zipFile.getEntry("Thumbnails/thumbnail.png");

            try (BufferedInputStream in = new BufferedInputStream(zipFile.getInputStream(entry))) {
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
     * Returns an array of MIME types that this thumbnailer accepts.
     * It covers various OpenOffice document formats as well as PDFs and potential OpenOffice files in zip format.
     *
     * @return An array of accepted MIME types.
     */
    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[] {
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
