package io.github.makbn.thumbnailer.thumbnailers;

import io.github.makbn.thumbnailer.exception.ThumbnailerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TextThumbnailer extends AbstractThumbnailer {
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final Logger mLog = LogManager.getLogger("TextThumbnailer");

    @Override
    public void generateThumbnail(File input, File output) throws IOException, ThumbnailerException {


        String text = readFile(input);
        BufferedImage img = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        Font font = new Font("Arial", Font.PLAIN, 11);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        g2d.dispose();

        img = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);

        int textW = g2d.getFontMetrics().stringWidth(text);

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
        for (String line : lines)
            g2d.drawString(line, 0, y += g2d.getFontMetrics().getHeight());

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
            while ((line = br.readLine()) != null && linecount++ < 5) {
                text.append(line.replace("\n", ""));
            }
        }

        return charset.decode(charset.encode(text.toString())).toString();
    }


    public String[] getAcceptedMIMETypes() {
        return new String[]{
                "text/plain",
                "text/rtf",
        };
    }
}
