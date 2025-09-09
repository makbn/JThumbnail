package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import com.spire.doc.Document;
import com.spire.doc.documents.ImageType;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Dummy class for converting Text documents into Openoffice-Textfiles.
 * <p>
 * Tika could be used to detect ms-word-files, but quite a heavy library. Maybe it would be useful as a preperator as well?
 *
 * @see JODConverterThumbnailer
 */
@Component
@Slf4j
public class WordConverterThumbnailer extends AbstractThumbnailer {

    public WordConverterThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {

        log.debug(
                "Starting thumbnail generation for {} with {}",
                input.getName(),
                this.getClass().getName());

        // Create a Document object
        Document doc = new Document();

        try {
            log.trace("Loading document into RAM");
            // Load a Word document
            doc.loadFromFile(input.getAbsolutePath());

            log.trace("Document loaded, saving all pages to images");

            // Convert the whole document into individual buffered images
            BufferedImage[] pages = doc.saveToImages(ImageType.Bitmap);

            log.trace("Pages saved, getting first page and scaling for thumbnail");
            Image image = pages[0].getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);

            // Re-write the image with a different color space
            BufferedImage newImg = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            newImg.getGraphics().drawImage(image, 0, 0, null);

            log.debug("Writing {} thumbnail to {}", input.getName(), output.getAbsolutePath());
            ImageIO.write(newImg, FilenameUtils.getExtension(output.getName()), output);
        } catch (IOException e) {
            throw new ThumbnailException(e);
        } catch (Exception e) {
            log.error(
                    "Got an unexpected exception with message {}, throwing ThumbnailException",
                    e.getLocalizedMessage());
            throw new ThumbnailException(e);
        } finally {
            doc.close();
        }
    }

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailException {
        generateThumbnail(input, output);
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[] {
            "application/vnd.ms-word",
            "application/vnd.openxmlformats-officedocument.wordprocessingml",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/wordperfect",
        };
    }
}
