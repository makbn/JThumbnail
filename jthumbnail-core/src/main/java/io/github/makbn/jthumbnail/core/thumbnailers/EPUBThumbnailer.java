package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

/**
 * Thumbnailer for EPUB files.
 * Attempts to extract a cover image from the archive.
 */
@Component
@Slf4j
public class EPUBThumbnailer extends AbstractThumbnailer {

    public EPUBThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        log.debug("Generating EPUB thumbnail for {}", input.getName());

        byte[] coverBytes = null;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(input))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName().toLowerCase();
                // Simple heuristic: search for 'cover' in image files
                if (name.contains("cover") && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
                    coverBytes = IOUtils.toByteArray(zis);
                    zis.closeEntry();
                    break;
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new ThumbnailException("Failed to read EPUB file", e);
        }

        if (coverBytes != null) {
            try {
                BufferedImage cover = ImageIO.read(new ByteArrayInputStream(coverBytes));
                if (cover != null) {
                    BufferedImage scaled = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = scaled.createGraphics();
                    try {
                        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2.drawImage(cover, 0, 0, thumbWidth, thumbHeight, null);
                    } finally {
                        g2.dispose();
                    }
                    ImageIO.write(scaled, "png", output);
                    return;
                }
            } catch (IOException e) {
                log.warn("Found cover but failed to read it: {}", e.getMessage());
            }
        }

        // Fallback: Generic EPUB icon
        generateFallbackIcon(output);
    }

    private void generateFallbackIcon(File output) throws ThumbnailException {
        BufferedImage img = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(156, 39, 176)); // Purple
            g2.fillRect(0, 0, thumbWidth, thumbHeight);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("EPUB", 10, 30);
            
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString("Digital Book", 10, 50);

            // Draw book shape simulation
            g2.fillRect(30, 70, thumbWidth - 60, thumbHeight - 100);
            g2.setColor(new Color(123, 31, 162));
            g2.drawRect(30, 70, thumbWidth - 60, thumbHeight - 100);

        } finally {
            g2.dispose();
        }
        try {
            ImageIO.write(img, "png", output);
        } catch (IOException e) {
            throw new ThumbnailException("Failed to write EPUB fallback thumbnail", e);
        }
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{"application/epub+zip"};
    }
}
