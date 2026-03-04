package io.github.makbn.jthumbnail.storage;

import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobCompletionHandler;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * When a thumbnail job completed and has S3 output metadata, uploads the
 * thumbnail file to the configured bucket/key.
 */
@Component
@ConditionalOnProperty(name = "jthumbnailer.storage.enabled", havingValue = "true")
@Slf4j
public class S3ThumbnailUploader implements ThumbnailJobCompletionHandler {

    private final S3ClientService s3Client;

    public S3ThumbnailUploader(S3ClientService s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public void onCompleted(ThumbnailJob job) throws IOException {
        if (!job.isS3Job() || job.getThumbnailPath() == null) {
            return;
        }
        Path local = Path.of(job.getThumbnailPath());
        if (!Files.isRegularFile(local)) {
            log.warn("Thumbnail file missing for S3 upload: {}", local);
            return;
        }
        String contentType = "image/png";
        if (job.getThumbnailPath().toLowerCase().endsWith(".jpg")
                || job.getThumbnailPath().toLowerCase().endsWith(".jpeg")) {
            contentType = "image/jpeg";
        }
        s3Client.uploadFile(job.getOutputBucket(), job.getOutputKey(), local, contentType);
        log.info(
                "Uploaded thumbnail for job {} to s3://{}/{}",
                job.getJobId(),
                job.getOutputBucket(),
                job.getOutputKey());
    }
}
