package io.github.makbn.jthumbnail.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class JThumbnailAutoConfigurationTest {

    @Test
    void autoConfigurationIsRegisteredInImportsFile() throws Exception {
        String resource = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertTrue(in != null, "AutoConfiguration.imports resource should exist");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                boolean found = reader.lines().anyMatch(line -> line.trim()
                        .equals("io.github.makbn.jthumbnail.autoconfigure.JThumbnailAutoConfiguration"));
                assertTrue(found, "JThumbnailAutoConfiguration should be listed in AutoConfiguration.imports");
            }
        }
    }
}
