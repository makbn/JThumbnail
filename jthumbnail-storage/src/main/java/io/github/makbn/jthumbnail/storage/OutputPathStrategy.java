package io.github.makbn.jthumbnail.storage;

import org.apache.commons.io.FilenameUtils;

/**
 * Computes output bucket and key for a thumbnail from source bucket/key and config.
 */
public final class OutputPathStrategy {

    private OutputPathStrategy() {}

    /**
     * Resolve output bucket and key from payload and storage config.
     * Thumbnail key is prefix + base name of source key + _thumb.png
     */
    public static String outputBucket(S3EventPayload payload, StorageProperties props) {
        return switch (props.outputStrategy()) {
            case SAME_BUCKET_PREFIX -> payload.bucket();
            case DIFFERENT_BUCKET -> props.outputBucket() != null ? props.outputBucket() : payload.bucket();
        };
    }

    public static String outputKey(S3EventPayload payload, StorageProperties props) {
        String prefix = props.outputPrefix() != null ? props.outputPrefix() : "thumbnails/";
        if (!prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        String base = FilenameUtils.getBaseName(payload.key());
        return prefix + base + "_thumb.png";
    }
}
