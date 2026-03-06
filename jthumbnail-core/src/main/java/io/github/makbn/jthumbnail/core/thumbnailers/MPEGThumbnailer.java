package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.util.FFmpegUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * MPEG thumbnailer using JavaCV to generate animated GIFs.
 */
@Component
@Slf4j
public class MPEGThumbnailer extends AbstractThumbnailer {

    public static final int MAX_NUM_FRAMES = 10;

    public MPEGThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        log.debug("Generating animated GIF thumbnail for {}", input.getName());
        try {
            FFmpegUtils.extractAnimatedThumbnail(input.getPath(), output.getPath(), MAX_NUM_FRAMES, thumbWidth, thumbHeight);
        } catch (IOException e) {
            throw new ThumbnailException("MPEG Thumbnailer failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "video/mp4",
                "video/MP2T",
                "video/x-matroska",
                "video/x-msvideo",
                "video/x-ms-wmv",
                "video/x-m4v",
                "video/webm",
                "video/quicktime",
                "video/3gpp"
        };
    }
}
