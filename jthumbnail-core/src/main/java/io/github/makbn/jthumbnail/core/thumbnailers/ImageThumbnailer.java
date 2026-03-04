package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @author Matt Akbarian (makbn)
 */
@Component
@Slf4j
public class ImageThumbnailer extends AbstractThumbnailer {

    public ImageThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        log.debug(
                "Starting thumbnail generation for {} with {}",
                input.getName(),
                this.getClass().getName());

        try {
            log.trace("Calling thumbnailator");
            Thumbnails.of(input)
                    .allowOverwrite(true)
                    .antialiasing(Antialiasing.ON)
                    .size(thumbWidth, thumbHeight)
                    .toFile(output);
            log.debug("Thumbnail generated for {} to {}", input.getName(), output.getAbsolutePath());
        } catch (IOException e) {
            log.error("Got an IOException", e);
            throw new ThumbnailException();
        }
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[] {"image/png", "image/jpeg", "image/tiff", "image/bmp", "image/jpg", "image/gif"};
    }
}
