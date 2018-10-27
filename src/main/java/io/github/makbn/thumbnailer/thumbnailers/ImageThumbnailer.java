package io.github.makbn.thumbnailer.thumbnailers;

import io.github.makbn.thumbnailer.ThumbnailerException;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-23
 */
public class ImageThumbnailer extends AbstractThumbnailer {

    private static final Logger logger = LoggerFactory.getLogger(ImageThumbnailer.class);

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        try {
            Thumbnails.of(input)
                    .allowOverwrite(true)
                    .antialiasing(Antialiasing.ON)
                    .size(thumbWidth, thumbHeight)
                    .toFile(output);
        } catch (IOException e) {
            logger.warn("ImageThumbnailer", e);
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
