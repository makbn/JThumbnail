package io.github.makbn.jthumbnail.core.thumbnailers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.FfmpegProperties;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class FfmpegThumbnailerTest {

    @TempDir
    Path tempDir;

    private FfmpegThumbnailer thumbnailer;
    private FfmpegProperties props;

    @BeforeEach
    void setUp() {
        ThumbnailProperties appProps = new ThumbnailProperties(400, 535);
        props = new FfmpegProperties("ffmpeg", "ffprobe", 1L, 10.0, 3, 0L, 1, true);
        thumbnailer = new FfmpegThumbnailer(appProps, props);
    }

    @Test
    void getAcceptedMIMETypesIncludesMp4MkvMovAvi() {
        String[] mimes = thumbnailer.getAcceptedMIMETypes();
        assertNotNull(mimes);
        assertEquals(9, mimes.length);
        List<String> list = List.of(mimes);
        assertEquals(
                List.of(
                        "video/mp4",
                        "video/x-matroska",
                        "video/quicktime",
                        "video/x-msvideo",
                        "video/MP2T",
                        "video/x-ms-wmv",
                        "video/x-m4v",
                        "video/webm",
                        "video/3gpp"),
                list);
    }

    @Test
    void generateThumbnailWhenDisabledThrows() throws Exception {
        FfmpegProperties disabled = new FfmpegProperties("ffmpeg", "ffprobe", 1L, 10.0, 3, 0L, 1, false);
        FfmpegThumbnailer t = new FfmpegThumbnailer(new ThumbnailProperties(400, 535), disabled);
        File in = tempDir.resolve("fake.mp4").toFile();
        Files.writeString(in.toPath(), "not a video");
        File out = tempDir.resolve("out.gif").toFile();

        try {
            t.generateThumbnail(in, out);
        } catch (ThumbnailException e) {
            assertEquals("FFmpeg thumbnailer is disabled", e.getMessage());
            return;
        }
        throw new AssertionError("Expected ThumbnailException");
    }

    @Test
    void computeFramePositionsSingleFrameUsesPercentage() {
        // 100s video, 10% -> 10s
        List<Double> pos = thumbnailer.computeFramePositions(100.0, 1);
        assertEquals(1, pos.size());
        assertEquals(10.0, pos.get(0), 0.01);
    }

    @Test
    void computeFramePositionsMultipleFramesEvenlySpaced() {
        List<Double> pos = thumbnailer.computeFramePositions(10.0, 3);
        assertEquals(3, pos.size());
        // (1/4)*10, (2/4)*10, (3/4)*10 -> 2.5, 5, 7.5
        assertEquals(2.5, pos.get(0), 0.01);
        assertEquals(5.0, pos.get(1), 0.01);
        assertEquals(7.5, pos.get(2), 0.01);
    }
}
