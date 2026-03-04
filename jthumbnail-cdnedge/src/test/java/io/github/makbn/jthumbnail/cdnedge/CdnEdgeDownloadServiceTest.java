package io.github.makbn.jthumbnail.cdnedge;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

class CdnEdgeDownloadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void hostAndExtensionFiltersApplied() throws Exception {
        CdnEdgeProperties props =
                new CdnEdgeProperties(true, List.of("localhost"), List.of("png"), 1024L * 1024L, Duration.ofSeconds(5));
        CdnEdgeDownloadService service = new CdnEdgeDownloadService(props);

        // valid URL served by a tiny HTTP server
        com.sun.net.httpserver.HttpServer server =
                com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/image.png", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "image/png");
            byte[] body = new byte[10];
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        int port = server.getAddress().getPort();

        File out = service.downloadToTemp("http://localhost:" + port + "/image.png");
        assertTrue(out.exists());
        assertTrue(out.length() > 0);

        server.stop(0);
    }

    @Test
    void rejectsDisallowedHost() {
        CdnEdgeProperties props = CdnEdgeProperties.withDefaults();
        CdnEdgeDownloadService service = new CdnEdgeDownloadService(props);
        assertThrows(Exception.class, () -> service.downloadToTemp("http://not-allowed.local/test.png"));
    }
}
