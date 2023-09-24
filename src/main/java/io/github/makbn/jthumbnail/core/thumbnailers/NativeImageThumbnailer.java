package io.github.makbn.jthumbnail.core.thumbnailers;


import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.util.ResizeImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * This class uses Java Image I/O (Java's internal Image Processing library) in order to resize images.
 * JAI can be extended with extra Readers, this Thumbnailer will use all available image readers.
 * <p>
 * Depends:
 * <li>JAI Image I/O Tools (optional, for TIFF support) (@see http://java.net/projects/imageio-ext/ - licence not gpl compatible I suspect ...)
 */
@Component
public class NativeImageThumbnailer extends AbstractThumbnailer {

    private static final Logger mLog = LogManager.getLogger("NativeImageThumbnailer");

    @Autowired
    public NativeImageThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }

    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);

        try {
            resizer.setInputImage(input);
            resizer.writeOutput(output);
        } catch (IOException e) {
            mLog.error(e);
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