package io.github.makbn.jthumbnail.storage;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * S3-compatible client: download object to local file, upload file to bucket/key.
 * Works with AWS S3 and MinIO via endpoint override.
 */
@Service
@ConditionalOnProperty(name = "jthumbnailer.storage.enabled", havingValue = "true")
@Slf4j
public class S3ClientService {

    private final S3Client s3Client;

    public S3ClientService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /** Download S3 object to a local file; returns the path. */
    public Path downloadToFile(String bucket, String key, Path targetFile) throws IOException {
        GetObjectRequest req =
                GetObjectRequest.builder().bucket(bucket).key(key).build();
        ResponseInputStream<GetObjectResponse> stream = s3Client.getObject(req);
        try (OutputStream out = Files.newOutputStream(targetFile)) {
            stream.transferTo(out);
        }
        log.debug("Downloaded s3://{}/{} to {}", bucket, key, targetFile);
        return targetFile;
    }

    /** Upload local file to S3. */
    public void uploadFile(String bucket, String key, Path localFile, String contentType) throws IOException {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType != null ? contentType : "image/png")
                .build();
        s3Client.putObject(req, RequestBody.fromFile(localFile));
        log.debug("Uploaded {} to s3://{}/{}", localFile, bucket, key);
    }
}
