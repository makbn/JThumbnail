package io.github.makbn.jthumbnail.core.thumbnailers;

import com.spire.doc.Document;
import com.spire.doc.documents.ImageType;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

/**
 * Dummy class for converting Text documents into Openoffice-Textfiles.
 * <p>
 * Tika could be used to detect ms-word-files, but quite a heavy library. Maybe it would be useful as a preperator as well?
 *
 * @see JODConverterThumbnailer
 */
@Component
public class WordConverterThumbnailer extends AbstractThumbnailer {

    public WordConverterThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {

        // Create a Document object
        Document doc = new Document();

        // Load a Word document
        doc.loadFromFile(input.getAbsolutePath());

        // Convert the whole document into individual buffered images
        BufferedImage[] pages = doc.saveToImages(ImageType.Bitmap);

        Image image = pages[0].getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);

        // Re-write the image with a different color space
        BufferedImage newImg = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        newImg.getGraphics().drawImage(image, 0, 0, null);

        try {
            ImageIO.write(newImg, FilenameUtils.getExtension(output.getName()), output);
        } catch (IOException e) {
            throw new ThumbnailerException(e);
        }
    }

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws IOException, ThumbnailerException {
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
