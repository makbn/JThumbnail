package io.github.makbn.jthumbnail.core.util;

import io.github.makbn.jthumbnail.core.model.ThumbnailConfig;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Applies {@link ThumbnailConfig} options on top of an already generated
 * thumbnail image. This runs after the underlying {@code Thumbnailer}
 * implementation has produced its output file and therefore works uniformly
 * for all providers.
 */
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ThumbnailPostProcessor {

    private ThumbnailPostProcessor() {}

    public static void applyConfig(File imageFile, ThumbnailConfig config) throws IOException {
        if (imageFile == null || config == null || config.isNoOp()) {
            return;
        }

        BufferedImage source = ImageIO.read(imageFile);
        if (source == null) {
            log.debug("ThumbnailPostProcessor: file {} is not a readable image, skipping", imageFile);
            return;
        }

        BufferedImage working = source;

        // Size & cropping
        if (config.getWidth() != null || config.getHeight() != null) {
            int targetW = config.getWidth() != null ? config.getWidth() : working.getWidth();
            int targetH = config.getHeight() != null ? config.getHeight() : working.getHeight();
            working = resizeWithCropMode(working, targetW, targetH, config);
        }

        // Rotation
        if (config.getRotationDegrees() != null && Math.abs(config.getRotationDegrees()) > 0.0001) {
            working = rotate(working, config.getRotationDegrees());
        }

        // Color filters
        if (config.getColorFilter() != null
                && config.getColorFilter() != ThumbnailConfig.ColorFilter.NONE) {
            working = applyColorFilter(working, config.getColorFilter());
        }

        // Border
        if (config.isAddBorder() && config.getBorderSize() != null && config.getBorderSize() > 0) {
            working = addBorder(working, config.getBorderSize(), parseColorOrDefault(
                    config.getBorderColor(), Color.BLACK));
        }

        // Text overlay
        if (config.getOverlayText() != null && !config.getOverlayText().isBlank()) {
            int fontSize = config.getOverlayTextSize() != null ? config.getOverlayTextSize() : 14;
            Color textColor =
                    parseColorOrDefault(config.getOverlayTextColor(), Color.WHITE);
            working = addOverlayText(working, config.getOverlayText(), fontSize, textColor);
        }

        String format = resolveOutputFormat(imageFile, config);
        if (!ImageIO.write(working, format, imageFile)) {
            log.warn("ThumbnailPostProcessor: ImageIO could not write format {}, falling back to PNG", format);
            ImageIO.write(working, "PNG", imageFile);
        }
    }

    private static BufferedImage resizeWithCropMode(
            BufferedImage src, int targetW, int targetH, ThumbnailConfig config) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();
        if (srcW == 0 || srcH == 0) {
            return src;
        }

        ThumbnailConfig.CropMode mode =
                config.getCropMode() != null ? config.getCropMode() : ThumbnailConfig.CropMode.FIT;

        double scale;
        if (mode == ThumbnailConfig.CropMode.FILL) {
            scale = Math.max((double) targetW / srcW, (double) targetH / srcH);
        } else {
            scale = Math.min((double) targetW / srcW, (double) targetH / srcH);
        }

        int scaledW = (int) Math.round(srcW * scale);
        int scaledH = (int) Math.round(srcH * scale);

        int x = (targetW - scaledW) / 2;
        int y = (targetH - scaledH) / 2;

        BufferedImage out = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = out.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, targetW, targetH);
            g2d.drawImage(src, x, y, scaledW, scaledH, null);
        } finally {
            g2d.dispose();
        }

        return out;
    }

    private static BufferedImage rotate(BufferedImage src, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int w = src.getWidth();
        int h = src.getHeight();
        int newW = (int) Math.floor(w * cos + h * sin);
        int newH = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            AffineTransform at = new AffineTransform();
            at.translate((newW - w) / 2.0, (newH - h) / 2.0);
            at.rotate(radians, w / 2.0, h / 2.0);
            g2d.drawRenderedImage(src, at);
        } finally {
            g2d.dispose();
        }
        return rotated;
    }

    private static BufferedImage applyColorFilter(
            BufferedImage src, ThumbnailConfig.ColorFilter filter) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;

                int nr = r;
                int ng = g;
                int nb = b;

                switch (filter) {
                    case GRAYSCALE -> {
                        int gray = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
                        nr = ng = nb = gray;
                    }
                    case SEPIA -> {
                        int tr = (int) Math.min(255, (0.393 * r + 0.769 * g + 0.189 * b));
                        int tg = (int) Math.min(255, (0.349 * r + 0.686 * g + 0.168 * b));
                        int tb = (int) Math.min(255, (0.272 * r + 0.534 * g + 0.131 * b));
                        nr = tr;
                        ng = tg;
                        nb = tb;
                    }
                    default -> {
                        // NONE already handled by caller.
                    }
                }

                int outArgb = (a << 24) | (nr << 16) | (ng << 8) | nb;
                out.setRGB(x, y, outArgb);
            }
        }
        return out;
    }

    private static BufferedImage addBorder(BufferedImage src, int borderSize, Color color) {
        int newW = src.getWidth() + 2 * borderSize;
        int newH = src.getHeight() + 2 * borderSize;
        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = out.createGraphics();
        try {
            g2d.setColor(color);
            g2d.fillRect(0, 0, newW, newH);
            g2d.drawImage(src, borderSize, borderSize, null);
        } finally {
            g2d.dispose();
        }
        return out;
    }

    private static BufferedImage addOverlayText(
            BufferedImage src, String text, int fontSize, Color color) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = out.createGraphics();
        try {
            g2d.drawImage(src, 0, 0, null);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int x = (src.getWidth() - textWidth) / 2;
            int y = src.getHeight() - fm.getDescent() - 4;

            g2d.drawString(text, x, y);
        } finally {
            g2d.dispose();
        }
        return out;
    }

    private static Color parseColorOrDefault(String hex, Color fallback) {
        if (hex == null || hex.isBlank()) {
            return fallback;
        }
        try {
            return Color.decode(hex.trim());
        } catch (NumberFormatException e) {
            log.debug("ThumbnailPostProcessor: could not parse color '{}', using fallback {}", hex, fallback);
            return fallback;
        }
    }

    private static String resolveOutputFormat(File imageFile, ThumbnailConfig config) {
        if (config.getOutputFormat() == null || config.getOutputFormat() == ThumbnailConfig.OutputFormat.AUTO) {
            String name = imageFile.getName();
            int idx = name.lastIndexOf('.');
            if (idx > 0 && idx < name.length() - 1) {
                return name.substring(idx + 1);
            }
            return "PNG";
        }
        return switch (config.getOutputFormat()) {
            case JPEG -> "JPEG";
            case PNG -> "PNG";
            case WEBP -> "WEBP";
            case AUTO -> "PNG";
        };
    }
}

