package io.github.makbn.jthumbnail.core.thumbnailers;


import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.util.ResizeImage;
import lombok.extern.slf4j.Slf4j;

/**
 * This class generates image thumbnails using native Java libraries.
 * This class extends the {@link AbstractThumbnailer} and overrides the methods
 * to generate a thumbnail from an input image and to provide accepted MIME types.
 * It utilizes the {@link ResizeImage} class to resize the images.
 */
@Slf4j
@Component
public class NativeImageThumbnailer extends AbstractThumbnailer {

    public NativeImageThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }

    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);

        try {
            resizer.setInputImage(input);
            resizer.writeOutput(output);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ThumbnailerException("File format could not be interpreted as image", e);
        }

    }

    /**
     * Get a List of accepted File Types.
     * Normally, these are: bmp, jpg, wbmp, jpeg, png, gif
     * The exact list may depend on the Java installation.
     *
     * @return MIME-Types
     */
    @Override
    public String[] getAcceptedMIMETypes() {
        return ImageIO.getReaderMIMETypes();
    }
}