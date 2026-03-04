package io.github.makbn.jthumbnail.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * S3-compatible storage connector config: bucket whitelist, file type filter,
 * output path strategy, and client settings (AWS S3 / MinIO).
 *
 * @param enabled              whether the storage connector is active
 * @param endpointOverride     override for MinIO or custom S3 (e.g. http://minio:9000)
 * @param region               AWS region or MinIO region placeholder
 * @param bucketWhitelist      only process events from these buckets (empty = all)
 * @param fileTypeInclude      allowed extensions (e.g. pdf, png, docx); empty = all
 * @param outputStrategy       SAME_BUCKET_PREFIX or DIFFERENT_BUCKET
 * @param outputPrefix         prefix for same-bucket (e.g. thumbnails/)
 * @param outputBucket         target bucket when strategy is DIFFERENT_BUCKET
 * @param webhookPath          HTTP path for webhook (e.g. /storage/events)
 * @param sqsQueueUrl          optional SQS queue URL for S3 event notifications
 */
@ConfigurationProperties(prefix = "jthumbnailer.storage", ignoreUnknownFields = true)
public record StorageProperties(
        @DefaultValue("false") boolean enabled,
        String endpointOverride,
        @DefaultValue("us-east-1") String region,
        @DefaultValue("{}") List<String> bucketWhitelist,
        @DefaultValue("{}") List<String> fileTypeInclude,
        @DefaultValue("SAME_BUCKET_PREFIX") OutputStrategy outputStrategy,
        @DefaultValue("thumbnails/") String outputPrefix,
        String outputBucket,
        @DefaultValue("/storage/events") String webhookPath,
        String sqsQueueUrl) {

    public enum OutputStrategy {
        /** Write thumbnail to same bucket with outputPrefix + derived key. */
        SAME_BUCKET_PREFIX,
        /** Write thumbnail to outputBucket with outputPrefix + derived key. */
        DIFFERENT_BUCKET
    }
}
