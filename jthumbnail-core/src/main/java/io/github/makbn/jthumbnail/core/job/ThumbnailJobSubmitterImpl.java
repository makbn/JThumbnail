package io.github.makbn.jthumbnail.core.job;

import io.github.makbn.jthumbnail.connector.api.JobProducer;
import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import io.github.makbn.jthumbnail.core.metrics.ThumbnailMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the public connector API. All built-in connectors use this for
 * consistent job submission and queue dispatch.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ThumbnailJobSubmitterImpl implements ThumbnailJobSubmitter {

    private final ThumbnailJobService jobService;
    private final ThumbnailMetrics metrics;

    @Autowired(required = false)
    private JobProducer jobProducer;

    @Override
    public String submit(String localFilePath) {
        ThumbnailJob job = jobService.createJob(localFilePath);
        enqueueIfAvailable(job);
        return job.getJobId();
    }

    @Override
    public String submitForWatcher(String localFilePath, String moveToProcessedDir, String moveToFailedDir) {
        ThumbnailJob job = jobService.createJobFromWatcher(localFilePath, moveToProcessedDir, moveToFailedDir);
        enqueueIfAvailable(job);
        return job.getJobId();
    }

    @Override
    public String submitForS3(
            String localFilePath, String sourceBucket, String sourceKey, String outputBucket, String outputKey) {
        ThumbnailJob job = jobService.createJobFromS3(localFilePath, sourceBucket, sourceKey, outputBucket, outputKey);
        enqueueIfAvailable(job);
        return job.getJobId();
    }

    private void enqueueIfAvailable(ThumbnailJob job) {
        if (jobProducer != null) {
            metrics.recordRequest();
            jobProducer.sendJob(job.getJobId());
            log.debug("Enqueued job {} for {}", job.getJobId(), job.getFilePath());
        }
    }
}
