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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Thumbnailer for Markdown files (.md).
 * Renders the first few lines of text as a preview.
 */
@Component
@Slf4j
public class MarkdownThumbnailer extends AbstractThumbnailer {

    public MarkdownThumbnailer(ThumbnailProperties appProperties) {
        super(appProperties);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailException {
        log.debug("Generating Markdown thumbnail for {}", input.getName());

        String text;
        try {
            text = readMarkdownContent(input);
        } catch (IOException e) {
            throw new ThumbnailException("Failed to read markdown file: " + e.getMessage(), e);
        }

        BufferedImage img = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Background
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, thumbWidth, thumbHeight);

            // "MD" Badge or icon simulation
            g2.setColor(new Color(240, 240, 240));
            g2.fillRect(0, 0, thumbWidth, 30);
            g2.setColor(new Color(100, 100, 100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString("MARKDOWN", 10, 20);

            // Content
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();

            List<String> lines = wrapText(text, g2, thumbWidth - 20);
            int y = 50;
            for (String line : lines) {
                if (y + fm.getHeight() > thumbHeight - 10) break;
                g2.drawString(line, 10, y);
                y += fm.getHeight();
            }
        } finally {
            g2.dispose();
        }

        try {
            ImageIO.write(img, "png", output);
        } catch (IOException e) {
            throw new ThumbnailException("Failed to write markdown thumbnail", e);
        }
    }

    private String readMarkdownContent(File input) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null && count < 100) {
                sb.append(line).append("\n");
                count++;
            }
        }
        return sb.toString();
    }

    private List<String> wrapText(String text, Graphics2D g2, int maxWidth) {
        List<String> lines = new ArrayList<>();
        FontMetrics fm = g2.getFontMetrics();
        String[] rawLines = text.split("\n");

        for (String rawLine : rawLines) {
            if (rawLine.isEmpty()) {
                lines.add("");
                continue;
            }

            StringBuilder currentLine = new StringBuilder();
            String[] words = rawLine.split(" ");
            for (String word : words) {
                String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
                if (fm.stringWidth(testLine) < maxWidth) {
                    currentLine = new StringBuilder(testLine);
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
            lines.add(currentLine.toString());
        }
        return lines;
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{"text/markdown", "text/x-markdown"};
    }
}
