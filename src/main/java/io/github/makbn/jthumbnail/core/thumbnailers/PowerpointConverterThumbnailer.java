package io.github.makbn.jthumbnail.core.thumbnailers;

import com.spire.presentation.Presentation;
import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerRuntimeException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Dummy class for converting Presentation documents into Openoffice-Textfiles.
 *
 * @see JODConverterThumbnailer
 */
@Component
public class PowerpointConverterThumbnailer extends AbstractThumbnailer {

    @Autowired
    public PowerpointConverterThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        Presentation ppt = new Presentation();
        try {
            ppt.loadFromFile(input.getAbsolutePath());
            //Save PPT document to images
            Image image = ppt.getSlides().get(0).saveAsImage().getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);
            //Re-write the image with a different color space
            BufferedImage newImg = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            newImg.getGraphics().drawImage(image, 0, 0, null);
            ImageIO.write(newImg, FilenameUtils.getExtension(output.getName()), output);

        } catch (Exception e) {
            throw new ThumbnailerRuntimeException(e);
        } finally {
            ppt.dispose();
        }

    }

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws ThumbnailerException {
        generateThumbnail(input, output);
    }

    /**
     * Get a List of accepted File Types.
     * All Presentation Office Formats that OpenOffice understands are accepted.
     * (ppt, pptx, pps, ppsx)
     *
     * @return MIME-Types
     * @see <a href="http://www.artofsolving.com/opensource/jodconverter/guide/supportedformats">...</a>
     */
    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",

        };
    }

}
