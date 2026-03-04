package io.github.makbn.jthumbnail.core.job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent thumbnail job for async processing with retry and dead-letter handling.
 */
@Entity
@Table(name = "thumbnail_job")
public class ThumbnailJob {

    @Id
    @Column(length = 36, updatable = false, nullable = false)
    private String jobId;

    @Column(nullable = false, length = 2048)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private JobStatus status = JobStatus.PENDING;

    @Column(nullable = false)
    private int retryCount = 0;

    @Column(length = 4096)
    private String errorMessage;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant completedAt;

    @Column(length = 2048)
    private String thumbnailPath;

    @Column(length = 256)
    private String sourceBucket;

    @Column(length = 2048)
    private String sourceKey;

    @Column(length = 256)
    private String outputBucket;

    @Column(length = 2048)
    private String outputKey;

    @Column(length = 2048)
    private String moveToProcessedDir;

    @Column(length = 2048)
    private String moveToFailedDir;

    public enum JobStatus {
        PENDING,
        PROCESSING,
        FAILED,
        COMPLETED
    }

    protected ThumbnailJob() {}

    public ThumbnailJob(String jobId, String filePath) {
        this.jobId = jobId;
        this.filePath = filePath;
        this.createdAt = Instant.now();
    }

    public static ThumbnailJob create(String filePath) {
        return new ThumbnailJob(UUID.randomUUID().toString(), filePath);
    }

    public String getJobId() {
        return jobId;
    }

    public String getFilePath() {
        return filePath;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
        if (status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
            this.completedAt = Instant.now();
        }
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getSourceBucket() {
        return sourceBucket;
    }

    public void setSourceBucket(String sourceBucket) {
        this.sourceBucket = sourceBucket;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }

    public String getOutputBucket() {
        return outputBucket;
    }

    public void setOutputBucket(String outputBucket) {
        this.outputBucket = outputBucket;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    /** True if this job was triggered from S3 and thumbnail should be uploaded back. */
    public boolean isS3Job() {
        return outputBucket != null && outputKey != null;
    }

    public String getMoveToProcessedDir() {
        return moveToProcessedDir;
    }

    public void setMoveToProcessedDir(String moveToProcessedDir) {
        this.moveToProcessedDir = moveToProcessedDir;
    }

    public String getMoveToFailedDir() {
        return moveToFailedDir;
    }

    public void setMoveToFailedDir(String moveToFailedDir) {
        this.moveToFailedDir = moveToFailedDir;
    }

    /** True if this job was created by the filesystem watcher and file should be moved on completion/failure. */
    public boolean isWatcherJob() {
        return moveToProcessedDir != null || moveToFailedDir != null;
    }
}
