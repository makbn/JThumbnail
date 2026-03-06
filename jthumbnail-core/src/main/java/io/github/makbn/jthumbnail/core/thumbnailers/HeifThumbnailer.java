package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.util.FFmpegUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Thumbnailer for HEIC/HEIF images using JavaCV (FFmpeg).
 */
@Component
@Slf4j
public class HeifThumbnailer extends AbstractThumbnailer {

    public HeifThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        log.debug("Generating HEIF thumbnail for {}", input.getName());
        FFmpegUtils.extractThumbnail(input, output, thumbWidth, thumbHeight);
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
            "image/heic",
            "image/heif",
            "image/heic-sequence",
            "image/heif-sequence"
        };
    }
}
