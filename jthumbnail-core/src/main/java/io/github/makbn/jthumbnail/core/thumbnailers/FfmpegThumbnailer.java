package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.FfmpegProperties;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Video thumbnailer using the system FFmpeg binary. Extracts frame(s) by timestamp or
 * percentage of duration. Supports MP4, MKV, MOV, AVI, WebM, etc. If FFmpeg is missing
 * or fails, {@link io.github.makbn.jthumbnail.core.ThumbnailerManager} will fall back to the next thumbnailer (e.g.
 * {@link MPEGThumbnailer}).
 */
@Component("ffmpegThumbnailer")
@Order(1)
@EnableConfigurationProperties({FfmpegProperties.class})
@Slf4j
public class FfmpegThumbnailer extends AbstractThumbnailer {

    private static final String[] VIDEO_MIMES = {
        "video/mp4",
        "video/x-matroska",
        "video/quicktime",
        "video/x-msvideo",
        "video/MP2T",
        "video/x-ms-wmv",
        "video/x-m4v",
        "video/webm",
        "video/3gpp"
    };

    private final FfmpegProperties ffmpeg;

    public FfmpegThumbnailer(ThumbnailProperties appProperties, FfmpegProperties ffmpeg) {
        super(appProperties);
        this.ffmpeg = ffmpeg;
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        if (!ffmpeg.enabled()) {
            throw new ThumbnailException("FFmpeg thumbnailer is disabled");
        }
        log.debug("Generating video thumbnail with FFmpeg for {}", input.getName());
        try {
            double durationSec = getDurationSeconds(input);
            if (ffmpeg.maxDurationSec() > 0 && durationSec > ffmpeg.maxDurationSec()) {
                throw new ThumbnailException(
                        "Video duration " + durationSec + "s exceeds max " + ffmpeg.maxDurationSec() + "s");
            }
            int count = Math.max(1, ffmpeg.multipleFrameCount());
            List<Double> positions = computeFramePositions(durationSec, count);
            extractFrames(input, output, positions);
        } catch (IOException e) {
            throw new ThumbnailException("FFmpeg failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ThumbnailException("FFmpeg interrupted", e);
        }
    }

    /**
     * Get duration in seconds using ffprobe.
     */
    double getDurationSeconds(File input) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                ffmpeg.ffprobePath(),
                "-v",
                "error",
                "-show_entries",
                "format=duration",
                "-of",
                "default=noprint_wrappers=1:nokey=1",
                input.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String out =
                IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8).trim();
        boolean ok = p.waitFor(30, TimeUnit.SECONDS) && p.exitValue() == 0;
        if (!ok || out.isEmpty()) {
            throw new IOException("ffprobe failed or no duration: " + out);
        }
        return Double.parseDouble(out);
    }

    /**
     * Compute N frame positions: by default use percentage; otherwise evenly spaced.
     */
    List<Double> computeFramePositions(double durationSec, int count) {
        List<Double> positions = new ArrayList<>(count);
        if (count == 1) {
            double sec;
            if (ffmpeg.defaultPositionPercent() != null && durationSec > 0) {
                sec = durationSec * (ffmpeg.defaultPositionPercent() / 100.0);
            } else {
                sec = (double) ffmpeg.defaultTimestampSec();
            }
            sec = Math.max(0, Math.min(sec, durationSec - 0.1));
            positions.add(sec);
            return positions;
        }
        for (int i = 0; i < count; i++) {
            double frac = (count == 1) ? 0.1 : (i + 1) / (double) (count + 1);
            double sec = durationSec * frac;
            sec = Math.max(0, Math.min(sec, durationSec - 0.1));
            positions.add(sec);
        }
        return positions;
    }

    private void extractFrames(File input, File output, List<Double> positions)
            throws IOException, InterruptedException {
        String basePath = output.getAbsolutePath();
        int lastDot = basePath.lastIndexOf('.');
        String ext = (lastDot > 0) ? basePath.substring(lastDot) : ".png";
        String baseNoExt = (lastDot > 0) ? basePath.substring(0, lastDot) : basePath;

        for (int i = 0; i < positions.size(); i++) {
            double sec = positions.get(i);
            String outPath = (i == 0) ? basePath : (baseNoExt + "_" + (i + 1) + ext);
            extractSingleFrame(input, sec, outPath);
        }
    }

    private void extractSingleFrame(File input, double timestampSec, String outputPath)
            throws IOException, InterruptedException {
        // -ss before -i for fast seek; -vframes 1; scale to thumb size; quality
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpeg.ffmpegPath());
        cmd.add("-ss");
        cmd.add(String.format(Locale.US, "%.2f", timestampSec));
        cmd.add("-i");
        cmd.add(input.getAbsolutePath());
        cmd.add("-vframes");
        cmd.add("1");
        cmd.add("-vf");
        cmd.add("scale=" + thumbWidth + ":" + thumbHeight + ":force_original_aspect_ratio=decrease");
        cmd.add("-q:v");
        cmd.add(String.valueOf(ffmpeg.frameQuality()));
        cmd.add("-y");
        cmd.add(outputPath);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String err = IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);
        boolean ok = p.waitFor(60, TimeUnit.SECONDS) && p.exitValue() == 0;
        if (!ok) {
            throw new IOException("ffmpeg exit " + p.exitValue() + ": " + err);
        }
        if (!Files.exists(java.nio.file.Path.of(outputPath))) {
            throw new IOException("ffmpeg did not create output: " + outputPath);
        }
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return VIDEO_MIMES.clone();
    }
}
