package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.util.FFmpegUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Thumbnailer for RAW image formats using JavaCV (FFmpeg).
 */
@Component
@Slf4j
public class RawThumbnailer extends AbstractThumbnailer {

    public RawThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        log.debug("Generating RAW thumbnail for {}", input.getName());
        FFmpegUtils.extractThumbnail(input, output, thumbWidth, thumbHeight);
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
            "image/x-adobe-dng",
            "image/x-canon-cr2",
            "image/x-nikon-nef",
            "image/x-sony-arw",
            "image/x-fuji-raf",
            "image/x-panasonic-rw2",
            "image/x-olympus-orf",
            "image/x-pentax-pef",
            "image/x-sigma-x3f",
            "image/x-dcraw"
        };
    }
}
