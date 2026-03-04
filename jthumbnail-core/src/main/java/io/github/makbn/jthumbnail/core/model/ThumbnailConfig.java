package io.github.makbn.jthumbnail.core.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Per-thumbnail configuration that allows callers to influence how the
 * generated thumbnail should look. All fields are optional; when unset,
 * the existing default behaviour of the engine is preserved.
 */
@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ThumbnailConfig {

    /** Desired output image format. AUTO keeps the engine's default. */
    public enum OutputFormat {
        AUTO,
        JPEG,
        PNG,
        WEBP
    }

    /** How to map the source aspect ratio into the requested size. */
    public enum CropMode {
        /** Keep entire image, fit inside target box, padding as needed. */
        FIT,
        /** Fill target box and center-crop overflow (common for avatars). */
        FILL
    }

    /** Simple color transforms. */
    public enum ColorFilter {
        NONE,
        GRAYSCALE,
        SEPIA
    }

    OutputFormat outputFormat;
    Integer width;
    Integer height;
    CropMode cropMode;
    Double rotationDegrees;
    ColorFilter colorFilter;

    boolean addBorder;
    Integer borderSize;
    /**
     * Border color as hex RGB (`#RRGGBB`). When {@code null}, defaults to black
     * if a border is requested.
     */
    String borderColor;

    /** Optional overlay text drawn on top of the thumbnail. */
    String overlayText;

    Integer overlayTextSize;
    /**
     * Overlay text color as hex RGB (`#RRGGBB`). When {@code null}, defaults to
     * white if text is requested.
     */
    String overlayTextColor;

    /**
     * Configuration which preserves the engine's current behaviour.
     */
    public static ThumbnailConfig defaultConfig() {
        return ThumbnailConfig.builder()
                .outputFormat(OutputFormat.AUTO)
                .width(null)
                .height(null)
                .cropMode(CropMode.FIT)
                .rotationDegrees(0.0)
                .colorFilter(ColorFilter.NONE)
                .addBorder(false)
                .borderSize(null)
                .borderColor(null)
                .overlayText(null)
                .overlayTextSize(null)
                .overlayTextColor(null)
                .build();
    }

    /**
     * Returns {@code true} when this configuration does not request any
     * transformation beyond the existing defaults. Callers can use this to
     * cheaply skip post-processing.
     */
    public boolean isNoOp() {
        boolean sizeUnspecified = width == null && height == null;
        boolean noRotation = rotationDegrees == null || Math.abs(rotationDegrees) < 0.0001;
        boolean noBorder = !addBorder;
        boolean noText = overlayText == null || overlayText.isBlank();
        boolean defaultFormat = outputFormat == null || outputFormat == OutputFormat.AUTO;
        boolean defaultCrop = cropMode == null || cropMode == CropMode.FIT;
        boolean defaultFilter = colorFilter == null || colorFilter == ColorFilter.NONE;
        return sizeUnspecified && noRotation && noBorder && noText && defaultFormat && defaultCrop && defaultFilter;
    }
}
