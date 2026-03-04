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
 * Adapter for WordPress-style media upload webhooks.
 * Supports payloads like: attachment.url, media.source_url, or root "url".
 */
@Component
@Order(0)
@ConditionalOnProperty(name = "jthumbnailer.webhook.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class WordPressWebhookAdapter implements WebhookAdapter {

    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "wordpress";
    }

    @Override
    public boolean canHandle(Map<String, String> headers, String rawBody) {
        if (rawBody == null || rawBody.isBlank()) return false;
        String source = headers.get("x-webhook-source");
        if (source != null && source.toLowerCase().contains("wordpress")) {
            return true;
        }
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            if (root == null || !root.isObject()) return false;
            return hasWordPressStyleUrl(root);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public WebhookJobRequest toJobRequest(Map<String, String> headers, String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            if (root == null || !root.isObject()) return null;
            String fileUrl = extractFileUrl(root);
            if (fileUrl == null || fileUrl.isBlank()) {
                log.debug("WordPress adapter: no media URL found in payload");
                return null;
            }
            String idempotencyKey = headers.get("x-idempotency-key");
            if (idempotencyKey == null) idempotencyKey = headers.get("x-request-id");
            String sourceId = extractSourceId(root);
            return WebhookJobRequest.builder()
                    .fileUrl(fileUrl.trim())
                    .idempotencyKey(idempotencyKey)
                    .sourceId(sourceId)
                    .build();
        } catch (Exception e) {
            log.warn("WordPress adapter failed: {}", e.getMessage());
            return null;
        }
    }

    private static boolean hasWordPressStyleUrl(JsonNode root) {
        return getString(root, "url") != null
                || (root.has("attachment") && getString(root.get("attachment"), "url") != null)
                || (root.has("media") && getString(root.get("media"), "source_url") != null)
                || (root.has("attachment_url") && root.get("attachment_url").isTextual());
    }

    private static String extractFileUrl(JsonNode root) {
        String u = getString(root, "url");
        if (u != null) return u;
        if (root.has("attachment")) {
            u = getString(root.get("attachment"), "url");
            if (u != null) return u;
            u = getString(root.get("attachment"), "source_url");
            if (u != null) return u;
        }
        if (root.has("media")) {
            u = getString(root.get("media"), "source_url");
            if (u != null) return u;
            u = getString(root.get("media"), "url");
            if (u != null) return u;
        }
        return getString(root, "attachment_url");
    }

    private static String extractSourceId(JsonNode root) {
        String id = getString(root, "id");
        if (id != null) return id;
        if (root.has("attachment") && root.get("attachment").has("id")) {
            JsonNode n = root.get("attachment").get("id");
            return n.isNumber() ? String.valueOf(n.asLong()) : n.asText();
        }
        if (root.has("media") && root.get("media").has("id")) {
            JsonNode n = root.get("media").get("id");
            return n.isNumber() ? String.valueOf(n.asLong()) : n.asText();
        }
        return null;
    }

    private static String getString(JsonNode node, String key) {
        if (node == null) return null;
        JsonNode n = node.get(key);
        return n != null && n.isTextual() ? n.asText() : null;
    }
}
