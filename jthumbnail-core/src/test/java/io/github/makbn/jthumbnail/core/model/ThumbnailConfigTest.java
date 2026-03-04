package io.github.makbn.jthumbnail.core.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ThumbnailConfigTest {

    @Test
    void defaultConfigIsNoOp() {
        ThumbnailConfig cfg = ThumbnailConfig.defaultConfig();
        assertTrue(cfg.isNoOp());
    }

    @Test
    void configWithAnyOverrideIsNotNoOp() {
        ThumbnailConfig resized = ThumbnailConfig.builder().width(200).build();
        assertFalse(resized.isNoOp());

        ThumbnailConfig rotated =
                ThumbnailConfig.builder().rotationDegrees(45.0).build();
        assertFalse(rotated.isNoOp());

        ThumbnailConfig border =
                ThumbnailConfig.builder().addBorder(true).borderSize(2).build();
        assertFalse(border.isNoOp());

        ThumbnailConfig text = ThumbnailConfig.builder().overlayText("x").build();
        assertFalse(text.isNoOp());

        ThumbnailConfig jpeg = ThumbnailConfig.builder()
                .outputFormat(ThumbnailConfig.OutputFormat.JPEG)
                .build();
        assertFalse(jpeg.isNoOp());

        ThumbnailConfig crop = ThumbnailConfig.builder()
                .cropMode(ThumbnailConfig.CropMode.FILL)
                .build();
        assertFalse(crop.isNoOp());

        ThumbnailConfig filtered = ThumbnailConfig.builder()
                .colorFilter(ThumbnailConfig.ColorFilter.GRAYSCALE)
                .build();
        assertFalse(filtered.isNoOp());
    }
}
