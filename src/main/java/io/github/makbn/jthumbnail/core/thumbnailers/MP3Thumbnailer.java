package io.github.makbn.jthumbnail.core.thumbnailers;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.properties.AppProperties;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-23
 */
@Component
@Slf4j
public class MP3Thumbnailer extends AbstractThumbnailer {

    public MP3Thumbnailer(AppProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        try {
            Mp3File song = new Mp3File(input.getPath());
            if (song.hasId3v2Tag()) {
                ID3v2 id3v2tag = song.getId3v2Tag();
                byte[] imageData = id3v2tag.getAlbumImage();
                // converting the bytes to an image
                BufferedImage img = getScaledBI(ImageIO.read(new ByteArrayInputStream(imageData)));
                ImageIO.write(img, "png", output);
            }

        } catch (UnsupportedTagException | InvalidDataException | IOException e) {
            log.warn("MP3Thumbnailer", e);
            throw new ThumbnailerException();
        }
    }

    @SuppressWarnings("Duplicates")
    private BufferedImage getScaledBI(BufferedImage org) {
        Image tmp = org.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);
        BufferedImage scaleBI = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = scaleBI.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return scaleBI;
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[] {"audio/mpeg", "audio/mp3", "audio/mp4", "audio/vnd.wav"};
    }
}
