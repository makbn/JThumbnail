package io.github.makbn.jthumbnail.webhook;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

class WordPressWebhookAdapterTest {

    private WordPressWebhookAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WordPressWebhookAdapter(new ObjectMapper());
    }

    @Test
    void canHandle_acceptsWhenHeaderPresent() {
        assertTrue(adapter.canHandle(Map.of("x-webhook-source", "wordpress"), "{}"));
    }

    @Test
    void canHandle_acceptsPayloadWithAttachmentUrl() {
        String json = "{\"attachment\":{\"url\":\"https://site.com/wp-content/uploads/file.pdf\"}}";
        assertTrue(adapter.canHandle(Map.of(), json));
        WebhookJobRequest req = adapter.toJobRequest(Map.of(), json);
        assertNotNull(req);
        assertTrue(req.getFileUrl().contains("file.pdf"));
    }

    @Test
    void toJobRequest_extractsMediaSourceUrl() {
        String json = "{\"media\":{\"source_url\":\"https://cdn.example.com/video.mp4\",\"id\":42}}";
        WebhookJobRequest req = adapter.toJobRequest(Map.of(), json);
        assertNotNull(req);
        assertEquals("https://cdn.example.com/video.mp4", req.getFileUrl());
        assertEquals("42", req.getSourceId());
    }
}
