package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailRuntimeException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.util.ResizeImage;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import com.spire.presentation.Presentation;

import java.io.File;

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
            var image = ppt.getSlides().get(0).saveAsImage();
            ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);

            resizer.setInputImage(image);
            resizer.setResizeMethod(ResizeImage.RESIZE_FIT_BOTH_DIMENSIONS);
            log.debug("Writing {} thumbnail to {}", input.getName(), output.getAbsolutePath());
            resizer.writeOutput(output, FilenameUtils.getExtension(output.getName()));
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
