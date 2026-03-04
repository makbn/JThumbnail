package io.github.makbn.jthumbnail.core.properties;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for FFmpeg-based video thumbnail extraction.
 *
 * @param ffmpegPath         Path to ffmpeg binary (e.g. "ffmpeg" or "/usr/bin/ffmpeg").
 * @param ffprobePath        Path to ffprobe binary (e.g. "ffprobe" or "/usr/bin/ffprobe").
 * @param defaultTimestampSec Default position in seconds when no percentage is used. Used when
 *                            positionPercent is null or for absolute positioning.
 * @param defaultPositionPercent Position as percentage of duration (0–100). If set, overrides
 *                               defaultTimestampSec for single-frame extraction. Null = use timestamp.
 * @param frameQuality       FFmpeg image quality: 2–31 for mjpeg (2=best, 31=worst). Lower is better.
 * @param maxDurationSec     Max video duration in seconds to process; longer videos are rejected (0 = no limit).
 * @param multipleFrameCount If &gt; 1, extract this many frames (at evenly spaced positions). First frame
 *                            is written to the main output; others to output_2, output_3, etc.
 * @param enabled            When false, this thumbnailer is effectively disabled (throws so fallback is used).
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.ffmpeg", ignoreUnknownFields = false)
public record FfmpegProperties(
        @NotNull @DefaultValue("ffmpeg") String ffmpegPath,
        @NotNull @DefaultValue("ffprobe") String ffprobePath,
        @PositiveOrZero @DefaultValue("1") long defaultTimestampSec,
        @DecimalMin("0") @DecimalMax("100") @DefaultValue("10") Double defaultPositionPercent,
        @Positive @DefaultValue("3") int frameQuality,
        @PositiveOrZero @DefaultValue("0") long maxDurationSec,
        @Positive @DefaultValue("1") int multipleFrameCount,
        @DefaultValue("true") boolean enabled) {}
