package io.github.makbn.jthumbnail.core.thumbnailers;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.properties.ThumbnailProperties;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

/**
 * Thumbnailer for ZIP archives (.zip).
 * Lists the first few files found within the archive.
 */
@Component
@Slf4j
public class ZipThumbnailer extends AbstractThumbnailer {

    public ZipThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        log.debug("Generating ZIP thumbnail for {}", input.getName());

        List<String> entries = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(input))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null && entries.size() < 10) {
                entries.add(ze.getName());
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new ThumbnailException("Failed to read ZIP file: " + e.getMessage(), e);
        }

        BufferedImage img = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background
            g2.setColor(new Color(250, 250, 250));
            g2.fillRect(0, 0, thumbWidth, thumbHeight);

            // ZIP Header
            g2.setColor(new Color(255, 193, 7)); // Amber/Yellow
            g2.fillRect(0, 0, thumbWidth, 30);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("ZIP ARCHIVE", 10, 20);

            // Entries
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();

            int y = 50;
            if (entries.isEmpty()) {
                g2.drawString("(Empty Archive)", 10, y);
            } else {
                for (String entry : entries) {
                    if (y + fm.getHeight() > thumbHeight - 20) {
                        g2.drawString("...", 10, y);
                        break;
                    }
                    // Draw a small icon simulation
                    g2.setColor(Color.GRAY);
                    g2.fillRect(10, y - 8, 8, 10);
                    g2.setColor(Color.DARK_GRAY);
                    
                    String displayName = entry;
                    if(fm.stringWidth(displayName) > thumbWidth - 30){
                        // Truncate if too long
                        while(fm.stringWidth(displayName + "...") > thumbWidth - 30 && !displayName.isEmpty()){
                            displayName = displayName.substring(0, displayName.length() - 1);
                        }
                        displayName += "...";
                    }

                    g2.drawString(displayName, 25, y);
                    y += fm.getHeight() + 2;
                }
            }
        } finally {
            g2.dispose();
        }

        try {
            ImageIO.write(img, "png", output);
        } catch (IOException e) {
            throw new ThumbnailException("Failed to write ZIP thumbnail", e);
        }
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{"application/zip", "application/x-zip-compressed"};
    }
}
