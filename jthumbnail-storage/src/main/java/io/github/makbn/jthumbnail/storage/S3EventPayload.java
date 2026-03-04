package io.github.makbn.jthumbnail.storage;

import java.util.Map;

/**
 * Parsed S3 object-created event: bucket, key, and optional metadata.
 */
public record S3EventPayload(
        String bucket, String key, String eTag, long size, String eventName, Map<String, String> metadata) {}
