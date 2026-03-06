package io.github.makbn.jthumbnail.core.util;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utility for extracting thumbnails using JavaCV (FFmpeg).
 */
@Slf4j
@UtilityClass
public class FFmpegUtils {

    /**
     * Extracts a single frame from a video/image file and saves it as a PNG thumbnail.
     *
     * @param input       the input file (video, HEIC, RAW, etc.)
     * @param output      the output thumbnail file
     * @param thumbWidth  desired width
     * @param thumbHeight desired height
     * @throws ThumbnailException if extraction fails
     */
    public static void extractThumbnail(File input, File output, int thumbWidth, int thumbHeight)
            throws ThumbnailException {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {
            grabber.start();
            Frame frame = grabber.grabImage();
            if (frame == null) {
                throw new ThumbnailException("Could not grab image from " + input.getName());
            }

            try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                BufferedImage bi = converter.convert(frame);
                if (bi == null) {
                    throw new ThumbnailException("Could not convert frame to BufferedImage for " + input.getName());
                }

                BufferedImage scaled = scaleImage(bi, thumbWidth, thumbHeight);
                ImageIO.write(scaled, "png", output);
            }
            grabber.stop();
        } catch (IOException e) {
            throw new ThumbnailException("FFmpeg (JavaCV) failed for " + input.getName() + ": " + e.getMessage(), e);
        }
    }

    public static void extractAnimatedThumbnail(String inputPath, String outputPath, int maxNumFrames, int thumbWidth, int thumbHeight) throws IOException {
        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(inputPath);
             ImageOutputStream output = new FileImageOutputStream(new File(outputPath));
             Java2DFrameConverter converter = new Java2DFrameConverter()) {

            frameGrabber.start();

            int frameCount = frameGrabber.getLengthInFrames();
            GifSequenceWriter gifSequenceWriter = null;

            // Extract up to maxNumFrames frames for the animated GIF
            int numFrames = Math.min(maxNumFrames, frameCount);
            for (int i = 0; i < numFrames; i++) {
                int frameIndex = (i * (frameCount / numFrames));
                frameGrabber.setFrameNumber(frameIndex);
                Frame frame = frameGrabber.grabImage();
                if (frame != null) {
                    BufferedImage bi = converter.convert(frame);
                    if (bi == null) continue;
                    if (gifSequenceWriter == null) {
                        gifSequenceWriter = new GifSequenceWriter(output, bi.getType(), 500, true);
                    }
                    gifSequenceWriter.writeToSequence(FFmpegUtils.getScaledBI(bi, thumbWidth, thumbHeight));
                }
            }

            if (gifSequenceWriter != null) {
                gifSequenceWriter.close();
            }
            frameGrabber.stop();
        }
    }

    private static BufferedImage scaleImage(BufferedImage org, int thumbWidth, int thumbHeight) {
        Image tmp = org.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);
        BufferedImage scaleBI = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaleBI.createGraphics();
        try {
            g2d.drawImage(tmp, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        return scaleBI;
    }

    public static BufferedImage getScaledBI(BufferedImage org, int thumbWidth, int thumbHeight) {
        Image tmp = org.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);
        BufferedImage scaleBI = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaleBI.createGraphics();
        try {
            g2d.drawImage(tmp, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        return scaleBI;
    }
}
