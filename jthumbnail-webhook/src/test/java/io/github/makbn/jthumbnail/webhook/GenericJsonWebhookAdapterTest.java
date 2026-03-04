package io.github.makbn.jthumbnail.webhook;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

class GenericJsonWebhookAdapterTest {

    private GenericJsonWebhookAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GenericJsonWebhookAdapter(new ObjectMapper());
    }

    @Test
    void canHandle_acceptsValidJson() {
        assertTrue(adapter.canHandle(Map.of(), "{\"fileUrl\":\"https://x.com/f.pdf\"}"));
        assertFalse(adapter.canHandle(Map.of(), ""));
        assertFalse(adapter.canHandle(Map.of(), "not json"));
    }

    @Test
    void toJobRequest_extractsFileUrl() {
        WebhookJobRequest req = adapter.toJobRequest(Map.of(), "{\"fileUrl\":\"https://example.com/doc.pdf\"}");
        assertNotNull(req);
        assertEquals("https://example.com/doc.pdf", req.getFileUrl());
    }

    @Test
    void toJobRequest_fallsBackToUrl() {
        WebhookJobRequest req = adapter.toJobRequest(Map.of(), "{\"url\":\"https://example.com/alt.pdf\"}");
        assertNotNull(req);
        assertEquals("https://example.com/alt.pdf", req.getFileUrl());
    }

    @Test
    void toJobRequest_returnsNullWhenNoUrl() {
        WebhookJobRequest req = adapter.toJobRequest(Map.of(), "{\"id\":\"123\"}");
        assertNull(req);
    }
}
