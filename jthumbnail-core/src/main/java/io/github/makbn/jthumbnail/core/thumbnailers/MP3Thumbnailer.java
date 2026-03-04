package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import io.github.makbn.jthumbnail.core.util.ResizeImage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author Matt Akbarian (makbn)
 */
@Component
@Slf4j
public class MP3Thumbnailer extends AbstractThumbnailer {

    public MP3Thumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        log.debug(
                "Starting conversion of {} using {}",
                input.getName(),
                this.getClass().getName());
        try {
            log.trace("Loading document into RAM");
            Mp3File song = new Mp3File(input.getPath());
            if (song.hasId3v2Tag()) {
                ID3v2 id3v2tag = song.getId3v2Tag();
                byte[] imageData = id3v2tag.getAlbumImage();
                // converting the bytes to an image
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));

                ResizeImage resizer = new ResizeImage(thumbWidth, thumbHeight);
                resizer.setInputImage(img);
                log.debug("Writing {} thumbnail to {}", input.getName(), output.getAbsolutePath());
                resizer.writeOutput(output, "PNG");
            } else {
                log.info("MP3 file does not have Id3v2 tags, no thumbnail generated");
            }

        } catch (UnsupportedTagException | InvalidDataException | IOException e) {
            log.warn("MP3Thumbnailer", e);
            throw new ThumbnailException();
        }
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[] {"audio/mpeg", "audio/mp3", "audio/mp4", "audio/vnd.wav"};
    }
}
