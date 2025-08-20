package io.github.makbn.jthumbnail.core.thumbnailers;

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;

@Component
public class TextThumbnailer extends AbstractThumbnailer {
    private static final Charset charset = StandardCharsets.UTF_8;

    public TextThumbnailer(AppSettings appSettings) {
        super(appSettings);
    }

    @Override
    public void generateThumbnail(File input, File output) throws ThumbnailerException {

        String text;
        try {
            text = readFile(input);
        } catch (IOException e) {
            throw new ThumbnailerException(e);
        }

        BufferedImage img = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = img.createGraphics();

        Font font = new Font("Arial", Font.PLAIN, 11);
        graphics.setFont(font);

        graphics.dispose();

        img = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);
        graphics = img.createGraphics();
        graphics.setPaint (Color.WHITE);
        graphics.fillRect (0, 0, thumbWidth, thumbHeight);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setFont(font);
        FontMetrics fm = graphics.getFontMetrics();
        graphics.setColor(Color.BLACK);

        int textW = graphics.getFontMetrics().stringWidth(text);


        int lineCount = Math.max(1, textW / thumbWidth);

        int cc = text.length() / lineCount;

        int index = 0;
        ArrayList<String> lines = new ArrayList<>();

        while (index < text.length()) {
            String sub = text.substring(index, Math.min(index + cc, text.length()));
            lines.add(sub);
            index += cc;
        }

        int y = fm.getAscent();
        for (String line : lines) {
            y += graphics.getFontMetrics().getHeight();
            graphics.drawString(line, 0, y);
        }

        try {
            ImageIO.write(img, "png", output);
        } catch (IOException e) {
            throw new ThumbnailerException(e);
        }
    }

    private String readFile(File input) throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8))) {
            String line;
            int linecount = 0;
            while ((line = br.readLine()) != null && linecount++ < 50) {
                text.append(line.replace("\n", ""));
            }
        }

        return charset.decode(charset.encode(text.toString())).toString();
    }

    @Override
    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "text/plain",
                "text/rtf",
        };
    }
}
