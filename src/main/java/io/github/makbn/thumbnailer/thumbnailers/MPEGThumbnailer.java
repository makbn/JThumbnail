package io.github.makbn.thumbnailer.thumbnailers;


import io.github.makbn.thumbnailer.ThumbnailerException;
import io.github.makbn.thumbnailer.util.GifSequenceWriter;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class MPEGThumbnailer extends AbstractThumbnailer {

    private static final Logger logger = LoggerFactory.getLogger(MPEGThumbnailer.class);

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {
        try {
            getThumb(input.getPath(), output.getPath());
        } catch (Exception e) {
            logger.warn("MPEGThumbnailer", e);
            throw new ThumbnailerException();
        }
    }

    /**
     * get thumbnail from multimedia files
     *
     * @throws IOException
     * @throws InterruptedException
     * @Author Iman Akbari
     */
    public void getThumb(String inputPath, String outputPath)
            throws IOException, InterruptedException, FrameGrabber.Exception {
        FFmpegFrameGrabber g = new FFmpegFrameGrabber(inputPath);
        ImageOutputStream output = new FileImageOutputStream(new File(outputPath));

        g.setFormat("mp4");
        g.start();
        int frame_count = g.getLengthInFrames();

        GifSequenceWriter gifSequenceWriter = null;

        for (int ig = 0; ig < frame_count; ig += g.getLengthInFrames() / 10) {
            if (ig > 0) g.setFrameNumber(ig);

            BufferedImage bi = createImageFromBytes(g.grabImage().data.array());

            if (gifSequenceWriter == null)
                gifSequenceWriter = new GifSequenceWriter(output, bi.getType(), 500, true);
            gifSequenceWriter.writeToSequence(getScaledBI(bi));
        }

        g.stop();
        gifSequenceWriter.close();
    }


    /**
     * Get a List of accepted File Types.
     * Only PDF Files are accepted.
     *
     * @return MIME-Types
     */
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
            throw new RuntimeException(e);
        }
    }
}
