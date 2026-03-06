package io.github.makbn.jthumbnail.core.thumbnailers;

import org.apache.tika.Tika;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class BaseThumbnailerTest {
    Tika tika = new Tika();

    protected void assertOutputIsJpeg(File file) throws IOException {
        assertNotNull(ImageIO.read(file));

        String mimeType = tika.detect(file);
        assertTrue(mimeType.startsWith("image/"));
    }
}
