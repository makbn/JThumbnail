package io.github.makbn.jthumbnail.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Parses S3 object-created events from AWS S3 (SQS/SNS) or MinIO webhook JSON.
 */
@Component
@ConditionalOnProperty(name = "jthumbnailer.storage.enabled", havingValue = "true")
public class S3EventParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Parse JSON payload into one or more S3 event payloads. Supports AWS S3
     * event format (Records array) and MinIO-style single event.
     */
    public List<S3EventPayload> parse(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode records = root.path("Records");
            if (records.isArray()) {
                return StreamSupport.stream(records.spliterator(), false)
                        .map(this::recordToPayload)
                        .filter(p -> p != null)
                        .collect(Collectors.toList());
            }
            S3EventPayload single = objectEventToPayload(root);
            if (single != null) {
                return List.of(single);
            }
            return Collections.emptyList();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid S3 event JSON: " + e.getMessage(), e);
        }
    }

    private S3EventPayload recordToPayload(JsonNode record) {
        JsonNode s3 = record.path("s3");
        if (s3.isMissingNode()) {
            return null;
        }
        String bucket = s3.path("bucket").path("name").asText(null);
        JsonNode obj = s3.path("object");
        String key = obj.path("key").asText(null);
        if (bucket == null || key == null) {
            return null;
        }
        String eTag = obj.path("eTag").asText(null);
        long size = obj.path("size").asLong(0);
        String eventName = record.path("eventName").asText("");
        return new S3EventPayload(bucket, key, eTag, size, eventName, Map.of());
    }

    private S3EventPayload objectEventToPayload(JsonNode root) {
        String bucketName = null;
        JsonNode b = root.path("bucket");
        if (!b.isMissingNode()) {
            bucketName = b.isTextual() ? b.asText() : b.path("name").asText(null);
        }
        if (bucketName == null) {
            bucketName = root.path("Bucket").asText(null);
        }
        String keyStr = root.path("Key").asText(null);
        if (keyStr == null) {
            keyStr = root.path("key").asText(null);
        }
        if (bucketName == null || keyStr == null) {
            return null;
        }
        return new S3EventPayload(
                bucketName,
                keyStr,
                root.path("eTag").asText(null),
                root.path("size").asLong(0),
                root.path("EventName").asText(root.path("eventName").asText("ObjectCreated:Put")),
                Map.of());
    }
}
