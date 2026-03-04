package io.github.makbn.jthumbnail.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Adapter for generic JSON payloads: expects "fileUrl" at root or configurable path.
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "jthumbnailer.webhook.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class GenericJsonWebhookAdapter implements WebhookAdapter {

    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "generic-json";
    }

    @Override
    public boolean canHandle(Map<String, String> headers, String rawBody) {
        if (rawBody == null || rawBody.isBlank()) return false;
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            return root != null && root.isObject();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public WebhookJobRequest toJobRequest(Map<String, String> headers, String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            if (root == null || !root.isObject()) return null;
            String fileUrl = getString(root, "fileUrl");
            if (fileUrl == null) {
                fileUrl = getString(root, "url");
            }
            if (fileUrl == null) {
                fileUrl = getString(root, "source_url");
            }
            if (fileUrl == null || fileUrl.isBlank()) {
                log.debug("Generic JSON: no fileUrl/url/source_url in payload");
                return null;
            }
            String idempotencyKey = headers.get("x-idempotency-key");
            if (idempotencyKey == null) idempotencyKey = headers.get("x-request-id");
            String sourceId = getString(root, "id");
            if (sourceId == null) sourceId = getString(root, "eventId");
            return WebhookJobRequest.builder()
                    .fileUrl(fileUrl.trim())
                    .idempotencyKey(idempotencyKey)
                    .sourceId(sourceId)
                    .build();
        } catch (Exception e) {
            log.warn("Generic JSON adapter failed: {}", e.getMessage());
            return null;
        }
    }

    private static String getString(JsonNode node, String key) {
        JsonNode n = node.get(key);
        return n != null && n.isTextual() ? n.asText() : null;
    }
}
