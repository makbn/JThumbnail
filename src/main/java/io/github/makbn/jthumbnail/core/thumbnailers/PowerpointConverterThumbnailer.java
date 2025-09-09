package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailRuntimeException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import com.spire.presentation.Presentation;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Dummy class for converting Presentation documents into Openoffice-Textfiles.
 *
 * @see JODConverterThumbnailer
 */
@Component
@Slf4j
public class PowerpointConverterThumbnailer extends AbstractThumbnailer {

    public PowerpointConverterThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        log.debug(
                "Starting thumbnail generation for {} with {}",
                input.getName(),
                this.getClass().getName());

        Presentation ppt = new Presentation();
        try {
            log.trace("Loading document into RAM");
            ppt.loadFromFile(input.getAbsolutePath());

            log.trace("Document loaded, saving first slide as image and rescale");
            // Save PPT document to images
            Image image =
                    ppt.getSlides().get(0).saveAsImage().getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);
            // Re-write the image with a different color space
            BufferedImage newImg = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            newImg.getGraphics().drawImage(image, 0, 0, null);

            log.debug("Writing {} thumbnail to {}", input.getName(), output.getAbsolutePath());
            ImageIO.write(newImg, FilenameUtils.getExtension(output.getName()), output);

        } catch (Exception e) {
            throw new ThumbnailRuntimeException(e);
        } finally {
            ppt.dispose();
        }
    }

    @Override
    public void generateThumbnail(File input, File output, String mimeType) throws ThumbnailException {
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
        return new String[] {
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        };
    }
}
