package io.github.makbn.jthumbnail.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;

import javax.imageio.ImageIO;

/**
 * End-to-end test for the thumbnail webservice: upload a file, poll status, download thumbnail.
 * Uses embedded server and test profile (no LibreOffice).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = WebserviceTestApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.main.allow-circular-references=true")
class ThumbnailWebE2ETest {

    @Autowired
    @LocalServerPort
    int port;

    @Test
    void uploadImageThenPollStatusAndDownload() throws Exception {
        byte[] pngBytes = createMinimalPng();
        RestTemplate rest = new RestTemplate();
        String base = "http://localhost:" + port;

        // Upload
        HttpHeaders uploadHeaders = new HttpHeaders();
        uploadHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.ByteArrayResource(pngBytes) {
            @Override
            public String getFilename() {
                return "sample.png";
            }
        });
        ResponseEntity<JThumbnailApiResponseString> uploadResp = rest.postForEntity(
                base + "/", new HttpEntity<>(body, uploadHeaders), JThumbnailApiResponseString.class);

        assertEquals(HttpStatus.OK, uploadResp.getStatusCode());
        assertNotNull(uploadResp.getBody());
        assertTrue(uploadResp.getBody().getResult() != null
                && !uploadResp.getBody().getResult().isEmpty());
        String uid = uploadResp.getBody().getResult();

        // Poll status until GENERATED (200 with /download/ in result) or timeout
        Instant deadline = Instant.now().plus(Duration.ofSeconds(30));
        while (Instant.now().isBefore(deadline)) {
            ResponseEntity<JThumbnailApiResponseString> statusResp =
                    rest.getForEntity(base + "/?uid=" + uid, JThumbnailApiResponseString.class);
            assertTrue(
                    statusResp.getStatusCode() == HttpStatus.OK || statusResp.getStatusCode() == HttpStatus.ACCEPTED,
                    "status: " + statusResp.getStatusCode());
            assertNotNull(statusResp.getBody());
            if (statusResp.getBody().getResult() != null
                    && statusResp.getBody().getResult().startsWith("/download/")) {
                break;
            }
            if (statusResp.getStatusCode() == HttpStatus.NOT_FOUND) {
                break;
            }
            Thread.sleep(200);
        }

        // Download
        ResponseEntity<byte[]> downloadResp = rest.getForEntity(base + "/download/" + uid, byte[].class);
        assertTrue(
                downloadResp.getStatusCode() == HttpStatus.OK || downloadResp.getStatusCode() == HttpStatus.NO_CONTENT);
        if (downloadResp.getStatusCode() == HttpStatus.OK) {
            assertTrue(downloadResp.getBody() != null && downloadResp.getBody().length > 0);
        }
    }

    @Test
    void checkStatusWithInvalidUidReturns404() {
        RestTemplate rest = new RestTemplate();
        rest.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(HttpStatusCode statusCode) {
                return statusCode.is5xxServerError();
            }
        });
        String base = "http://localhost:" + port;
        ResponseEntity<JThumbnailApiResponseString> resp = rest.exchange(
                base + "/?uid=invalid-uid-12345", HttpMethod.GET, null, JThumbnailApiResponseString.class);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), resp.getBody().getCode());
    }

    private static byte[] createMinimalPng() throws Exception {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                img.setRGB(x, y, 0xFF_FF_00_00);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    @lombok.Data
    static class JThumbnailApiResponseString {
        private int code;
        private String message;
        private String result;
        private boolean error;
    }
}
