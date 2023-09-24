package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-23
 */
@Component
public class ImageThumbnailer extends AbstractThumbnailer {

    private static final Logger mLog = LogManager.getLogger("ImageThumbnailer");

    public ImageThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        try {
            Thumbnails.of(input)
                    .allowOverwrite(true)
                    .antialiasing(Antialiasing.ON)
                    .size(thumbWidth, thumbHeight)
                    .toFile(output);
        } catch (IOException e) {
            mLog.error(e);
            throw new ThumbnailerException();
        }
    }


    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "image/png",
                "image/jpeg",
                "image/tiff",
                "image/bmp",
                "image/jpg",
                "image/gif"
        };
    }
}
