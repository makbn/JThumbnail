package io.github.makbn.thumbnailer.thumbnailers;

import io.github.makbn.thumbnailer.config.AppSettings;
import io.github.makbn.thumbnailer.exception.ThumbnailerException;
import io.github.makbn.thumbnailer.exception.ThumbnailerRuntimeException;
import io.github.makbn.thumbnailer.util.GifSequenceWriter;
import lombok.extern.log4j.Log4j2;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * created by Mehdi Akbarian-Rastaghi 2018-10-22
 */
@Component
@Log4j2
public class MPEGThumbnailer extends AbstractThumbnailer {

    @Autowired
    public MPEGThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        try {
            getThumb(input.getPath(), output.getPath());
        } catch (IOException e) {
            throw new ThumbnailerException(e);
        }
    }

    /**
     * get thumbnail from multimedia files
     *
     */
    public void getThumb(String inputPath, String outputPath)
            throws IOException {

        try( FFmpegFrameGrabber g = new FFmpegFrameGrabber(inputPath);
             ImageOutputStream output = new FileImageOutputStream(new File(outputPath))) {
            g.setFormat("mp4");
            g.start();
            int frameCount = g.getLengthInFrames();

            GifSequenceWriter gifSequenceWriter = null;

            for (int ig = 0; ig < frameCount; ig += g.getLengthInFrames() / 10) {
                if (ig > 0) g.setFrameNumber(ig);

                BufferedImage bi = createImageFromBytes(g.grabImage().data.array());

                if (gifSequenceWriter == null)
                    gifSequenceWriter = new GifSequenceWriter(output, bi.getType(), 500, true);
                gifSequenceWriter.writeToSequence(getScaledBI(bi));
                g.stop();
                gifSequenceWriter.close();
            }
        }
    }


    /**
     * Get a List of accepted File Types.
     * Only PDF Files are accepted.
     *
     * @return MIME-Types
     */
    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "video/mp4",
                "video/MP2T",
                "video/x-msvideo",
                "video/x-ms-wmv",
                "video/x-m4v",
                "video/webm",
                "video/quicktime",
                "video/3gpp"

        };
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


    private BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
        throw new ThumbnailerRuntimeException("Error in generating thumbnail for MPEG file.");
    }
}
