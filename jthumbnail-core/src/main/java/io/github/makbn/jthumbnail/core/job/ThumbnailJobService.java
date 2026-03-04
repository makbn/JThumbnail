package io.github.makbn.jthumbnail.core.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbnailJobService {

    private final ThumbnailJobRepository repository;

    @Transactional
    public ThumbnailJob createJob(String filePath) {
        ThumbnailJob job = ThumbnailJob.create(filePath);
        repository.save(job);
        log.debug("Created job {} for file {}", job.getJobId(), filePath);
        return job;
    }

    /** Create a job for S3-originated content; caller must send to queue. */
    @Transactional
    public ThumbnailJob createJobFromS3(
            String localFilePath, String sourceBucket, String sourceKey, String outputBucket, String outputKey) {
        ThumbnailJob job = ThumbnailJob.create(localFilePath);
        job.setSourceBucket(sourceBucket);
        job.setSourceKey(sourceKey);
        job.setOutputBucket(outputBucket);
        job.setOutputKey(outputKey);
        repository.save(job);
        log.debug(
                "Created S3 job {} for s3://{}/{} -> s3://{}/{}",
                job.getJobId(),
                sourceBucket,
                sourceKey,
                outputBucket,
                outputKey);
        return job;
    }

    /** Create a job from the filesystem watcher; optionally move file to processed/failed dir when done. */
    @Transactional
    public ThumbnailJob createJobFromWatcher(String filePath, String moveToProcessedDir, String moveToFailedDir) {
        ThumbnailJob job = ThumbnailJob.create(filePath);
        job.setMoveToProcessedDir(moveToProcessedDir);
        job.setMoveToFailedDir(moveToFailedDir);
        repository.save(job);
        log.debug(
                "Created watcher job {} for {} (processed={}, failed={})",
                job.getJobId(),
                filePath,
                moveToProcessedDir,
                moveToFailedDir);
        return job;
    }

    public Optional<ThumbnailJob> findById(String jobId) {
        return repository.findById(jobId);
    }

    public List<ThumbnailJob> findByStatus(ThumbnailJob.JobStatus status) {
        return repository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<ThumbnailJob> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Optional<ThumbnailJob> markProcessing(String jobId) {
        return repository.findById(jobId).map(job -> {
            job.setStatus(ThumbnailJob.JobStatus.PROCESSING);
            return repository.save(job);
        });
    }

    @Transactional
    public Optional<ThumbnailJob> markCompleted(String jobId, String thumbnailPath) {
        return repository.findById(jobId).map(job -> {
            job.setStatus(ThumbnailJob.JobStatus.COMPLETED);
            job.setThumbnailPath(thumbnailPath);
            job.setErrorMessage(null);
            return repository.save(job);
        });
    }

    @Transactional
    public Optional<ThumbnailJob> markFailed(String jobId, String errorMessage) {
        return repository.findById(jobId).map(job -> {
            job.incrementRetryCount();
            job.setErrorMessage(errorMessage);
            job.setStatus(ThumbnailJob.JobStatus.FAILED);
            return repository.save(job);
        });
    }

    /** Mark job as PENDING again for retry; increment retry count and set error message. */
    @Transactional
    public Optional<ThumbnailJob> markPendingForRetry(String jobId, String errorMessage) {
        return repository.findById(jobId).map(job -> {
            job.incrementRetryCount();
            job.setErrorMessage(errorMessage);
            job.setStatus(ThumbnailJob.JobStatus.PENDING);
            return repository.save(job);
        });
    }
}
