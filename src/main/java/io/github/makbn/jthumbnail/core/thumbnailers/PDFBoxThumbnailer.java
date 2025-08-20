package io.github.makbn.jthumbnail.core.thumbnailers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerRuntimeException;
import io.github.makbn.jthumbnail.core.util.ResizeImage;

/**
 * Renders the first page of a PDF file into a thumbnail.
 */
@Component
public class PDFBoxThumbnailer extends AbstractThumbnailer {
    @Autowired
    public PDFBoxThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }
    private PDDocument getDocument(File input) throws IOException {
        return Loader.loadPDF(input);
    }
    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException, ThumbnailerRuntimeException {
        if(!Files.exists(input.toPath())){
            throw new ThumbnailerException("input file does not exist");
        }

        if (input.length() == 0)
            throw new ThumbnailerException("File is empty");
        FileUtils.deleteQuietly(output);

    try( PDDocument document = getDocument(input)) {
        BufferedImage tmpImage = writeImageFirstPage(document);

        if (tmpImage.getWidth() == thumbWidth) {
            ImageIO.write(tmpImage, "PNG", output);
        } else {
            ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);
            resizer.setResizeMethod(ResizeImage.RESIZE_FIT_BOTH_DIMENSIONS);
            resizer.setInputImage(tmpImage);
            resizer.writeOutput(output);
        }
        } catch (IllegalArgumentException e) {
            throw new ThumbnailerRuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new ThumbnailerException(e);
        }
    }

    /**
     * Loosely based on the commandline-Tool PDFImageWriter
     *
     * @param document to generate image from first page
     * @return generated image
     */
    private BufferedImage writeImageFirstPage(PDDocument document) throws IOException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        return pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
    }

    /**
     * Get a List of accepted File Types.
     * Only PDF Files are accepted.
     *
     * @return MIME-Types
     */
    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "application/pdf"
        };
    }


}
