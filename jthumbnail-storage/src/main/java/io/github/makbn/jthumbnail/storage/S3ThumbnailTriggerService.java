package io.github.makbn.jthumbnail.storage;

import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * On S3 event: parse, apply whitelist/file-type filter, download to temp file,
 * create job with S3 metadata, send to queue.
 */
@Service
@ConditionalOnProperty(name = "jthumbnailer.storage.enabled", havingValue = "true")
@Slf4j
public class S3ThumbnailTriggerService {

    private final StorageProperties props;
    private final S3EventParser parser;
    private final S3ClientService s3Client;
    private final ThumbnailJobSubmitter jobSubmitter;

    public S3ThumbnailTriggerService(
            StorageProperties props,
            S3EventParser parser,
            S3ClientService s3Client,
            ThumbnailJobSubmitter jobSubmitter) {
        this.props = props;
        this.parser = parser;
        this.s3Client = s3Client;
        this.jobSubmitter = jobSubmitter;
    }

    /**
     * Process raw event JSON (from webhook or SQS): parse, filter, download, enqueue job.
     */
    public int processEventPayload(String json) {
        List<S3EventPayload> payloads = parser.parse(json);
        int enqueued = 0;
        for (S3EventPayload p : payloads) {
            if (!isObjectCreated(p.eventName())) {
                log.debug("Skip non-create event: {}", p.eventName());
                continue;
            }
            if (!bucketAllowed(p.bucket())) {
                log.debug("Bucket not in whitelist: {}", p.bucket());
                continue;
            }
            if (!fileTypeAllowed(p.key())) {
                log.debug("File type not in include list: {}", p.key());
                continue;
            }
            try {
                enqueueOne(p);
                enqueued++;
            } catch (Exception e) {
                log.warn("Failed to enqueue s3://{}/{}: {}", p.bucket(), p.key(), e.getMessage());
            }
        }
        return enqueued;
    }

    private boolean isObjectCreated(String eventName) {
        if (eventName == null) return false;
        return eventName.startsWith("ObjectCreated:") || "s3:ObjectCreated:Put".equals(eventName);
    }

    private boolean bucketAllowed(String bucket) {
        List<String> wl = props.bucketWhitelist();
        if (wl == null || wl.isEmpty()) return true;
        return wl.contains(bucket);
    }

    private boolean fileTypeAllowed(String key) {
        List<String> include = props.fileTypeInclude();
        if (include == null || include.isEmpty()) return true;
        String ext = key.contains(".") ? key.substring(key.lastIndexOf('.') + 1).toLowerCase() : "";
        return include.stream().map(String::toLowerCase).anyMatch(ext::equals);
    }

    private void enqueueOne(S3EventPayload p) throws IOException {
        Path tempFile = Files.createTempFile(
                "s3-thumb-", "-" + Path.of(p.key()).getFileName().toString());
        try {
            s3Client.downloadToFile(p.bucket(), p.key(), tempFile);
        } catch (Exception e) {
            Files.deleteIfExists(tempFile);
            throw e;
        }
        String localPath = tempFile.toAbsolutePath().toString();
        String outBucket = OutputPathStrategy.outputBucket(p, props);
        String outKey = OutputPathStrategy.outputKey(p, props);

        jobSubmitter.submitForS3(localPath, p.bucket(), p.key(), outBucket, outKey);
        log.info("Enqueued S3 job for s3://{}/{}", p.bucket(), p.key());
    }
}
