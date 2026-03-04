package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.FfmpegProperties;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for FfmpegThumbnailer. Require ffmpeg/ffprobe on PATH.
 * Create a minimal MP4 with ffmpeg, then extract a thumbnail.
 */
class FfmpegThumbnailerIntegrationTest {

    @TempDir
    Path tempDir;

    private static boolean ffmpegAvailable;
    private static Path sampleVideo;

    @BeforeAll
    static void checkFfmpegAndCreateSampleVideo() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor(5, TimeUnit.SECONDS);
            ffmpegAvailable = (p.exitValue() == 0);
        } catch (Throwable t) {
            ffmpegAvailable = false;
        }

        if (ffmpegAvailable) {
            try {
                Path video = Files.createTempFile("jthumb_ffmpeg_test", ".mp4");
                video.toFile().deleteOnExit();
                ProcessBuilder create = new ProcessBuilder(
                        "ffmpeg",
                        "-y",
                        "-f",
                        "lavfi",
                        "-i",
                        "color=c=blue:s=320x240:d=3",
                        "-t",
                        "3",
                        "-pix_fmt",
                        "yuv420p",
                        video.toAbsolutePath().toString());
                create.redirectErrorStream(true);
                Process proc = create.start();
                proc.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
                boolean ok = proc.waitFor(15, TimeUnit.SECONDS) && proc.exitValue() == 0 && Files.size(video) > 0;
                if (ok) {
                    sampleVideo = video;
                }
            } catch (Throwable t) {
                sampleVideo = null;
            }
        }
    }

    @Test
    void extractSingleFrameAtTimestamp() throws Exception {
        assumeTrue(ffmpegAvailable && sampleVideo != null);
        ThumbnailProperties appProps = new ThumbnailProperties(400, 535);
        FfmpegProperties props = new FfmpegProperties("ffmpeg", "ffprobe", 0L, null, 3, 0L, 1, true);
        try (FfmpegThumbnailer thumbnailer = new FfmpegThumbnailer(appProps, props)) {

            File input = sampleVideo.toFile();
            File output = tempDir.resolve("thumb.gif").toFile();

            thumbnailer.generateThumbnail(input, output);

            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void extractSingleFrameAtPercentage() throws Exception {
        assumeTrue(ffmpegAvailable && sampleVideo != null);
        ThumbnailProperties appProps = new ThumbnailProperties(200, 200);
        FfmpegProperties props = new FfmpegProperties("ffmpeg", "ffprobe", 1L, 50.0, 5, 0L, 1, true);
        try (FfmpegThumbnailer thumbnailer = new FfmpegThumbnailer(appProps, props)) {

            File input = sampleVideo.toFile();
            File output = tempDir.resolve("thumb_pct.png").toFile();

            thumbnailer.generateThumbnail(input, output);

            assertTrue(output.exists());
            assertTrue(output.length() > 0);
        }
    }

    @Test
    void extractMultipleFrames() throws Exception {
        assumeTrue(ffmpegAvailable && sampleVideo != null);
        ThumbnailProperties appProps = new ThumbnailProperties(160, 120);
        FfmpegProperties props = new FfmpegProperties("ffmpeg", "ffprobe", 1L, 10.0, 3, 0L, 3, true);
        try (FfmpegThumbnailer thumbnailer = new FfmpegThumbnailer(appProps, props)) {

            File input = sampleVideo.toFile();
            File output = tempDir.resolve("multi.gif").toFile();

            thumbnailer.generateThumbnail(input, output);

            assertTrue(output.exists());
            assertTrue(output.length() > 0);
            File second = tempDir.resolve("multi_2.gif").toFile();
            File third = tempDir.resolve("multi_3.gif").toFile();
            assertTrue(second.exists() && second.length() > 0);
            assertTrue(third.exists() && third.length() > 0);
        }
    }

    @Test
    void maxDurationRejectsLongVideo() throws Exception {
        assumeTrue(ffmpegAvailable && sampleVideo != null);
        ThumbnailProperties appProps = new ThumbnailProperties(400, 535);
        FfmpegProperties props = new FfmpegProperties("ffmpeg", "ffprobe", 1L, 10.0, 3, 1L, 1, true);
        try (FfmpegThumbnailer thumbnailer = new FfmpegThumbnailer(appProps, props)) {

            File input = sampleVideo.toFile();
            File output = tempDir.resolve("reject.gif").toFile();

            try {
                thumbnailer.generateThumbnail(input, output);
            } catch (ThumbnailException e) {
                assertTrue(e.getMessage().contains("exceeds max"), "message: " + e.getMessage());
                return;
            }
            throw new AssertionError("Expected ThumbnailException for max duration");
        }
    }
}
