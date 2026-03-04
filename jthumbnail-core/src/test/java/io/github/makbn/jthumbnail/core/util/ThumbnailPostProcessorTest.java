package io.github.makbn.jthumbnail.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.makbn.jthumbnail.core.model.ThumbnailConfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import javax.imageio.ImageIO;

class ThumbnailPostProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void applyConfigNoOpWhenConfigNullOrNoOp() throws Exception {
        File input = tempDir.resolve("noop.png").toFile();
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "PNG", input);

        long originalSize = input.length();

        // null config must be ignored
        ThumbnailPostProcessor.applyConfig(input, null);

        // default (no-op) config must also be ignored
        ThumbnailPostProcessor.applyConfig(input, ThumbnailConfig.defaultConfig());

        assertEquals(originalSize, input.length());
    }

    @Test
    void applyConfigResizesAndAppliesGrayscale() throws Exception {
        File input = tempDir.resolve("in.png").toFile();
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "PNG", input);

        ThumbnailConfig config = ThumbnailConfig.builder()
                .width(40)
                .height(40)
                .cropMode(ThumbnailConfig.CropMode.FIT)
                .colorFilter(ThumbnailConfig.ColorFilter.GRAYSCALE)
                .build();

        ThumbnailPostProcessor.applyConfig(input, config);

        BufferedImage out = ImageIO.read(input);
        assertEquals(40, out.getWidth());
        assertEquals(40, out.getHeight());

        int rgb = out.getRGB(20, 20);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        assertTrue(r == g && g == b, "Pixel should be grayscale");
    }

    @Test
    void applyConfigWithFillCropModeAndRotationAndBorderAndText() throws Exception {
        File input = tempDir.resolve("full.png").toFile();
        BufferedImage img = new BufferedImage(80, 40, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "PNG", input);

        ThumbnailConfig config = ThumbnailConfig.builder()
                .width(60)
                .height(60)
                .cropMode(ThumbnailConfig.CropMode.FILL)
                .rotationDegrees(90.0)
                .addBorder(true)
                .borderSize(5)
                .borderColor("#FF0000")
                .overlayText("Hi")
                .overlayTextSize(10)
                .overlayTextColor("#00FF00")
                .build();

        ThumbnailPostProcessor.applyConfig(input, config);

        BufferedImage out = ImageIO.read(input);
        // 60x60 image with 5px border -> 70x70 final canvas
        assertEquals(70, out.getWidth());
        assertEquals(70, out.getHeight());
    }

    @Test
    void applyConfigWithInvalidColorsAndDifferentOutputFormats() throws Exception {
        File input = tempDir.resolve("format.png").toFile();
        BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "PNG", input);

        // Invalid border color should fall back to default without throwing.
        ThumbnailConfig invalidColor = ThumbnailConfig.builder()
                .addBorder(true)
                .borderSize(2)
                .borderColor("not-a-color")
                .build();
        ThumbnailPostProcessor.applyConfig(input, invalidColor);
        assertTrue(input.length() > 0);

        // AUTO format uses file extension, explicit JPEG / PNG / WEBP paths should be exercised
        for (ThumbnailConfig.OutputFormat fmt : new ThumbnailConfig.OutputFormat[] {
            ThumbnailConfig.OutputFormat.AUTO,
            ThumbnailConfig.OutputFormat.JPEG,
            ThumbnailConfig.OutputFormat.PNG,
            ThumbnailConfig.OutputFormat.WEBP
        }) {
            ThumbnailConfig cfg = ThumbnailConfig.builder()
                    .outputFormat(fmt)
                    .width(10)
                    .height(10)
                    .build();
            ThumbnailPostProcessor.applyConfig(input, cfg);
            assertTrue(input.length() > 0);
        }

        BufferedImage finalImg = ImageIO.read(input);
        assertNotNull(finalImg);
    }
}
