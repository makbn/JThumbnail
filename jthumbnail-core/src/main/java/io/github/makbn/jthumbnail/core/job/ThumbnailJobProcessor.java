package io.github.makbn.jthumbnail.core.job;

import io.github.makbn.jthumbnail.core.ThumbnailerManager;
import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailRuntimeException;
import io.github.makbn.jthumbnail.core.metrics.ThumbnailMetrics;
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeDetector;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Shared processor for thumbnail jobs. Used by Kafka and AMQP consumers.
 * Performs the actual thumbnail generation and returns whether to retry or send to DLQ.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ThumbnailJobProcessor {

    private final ThumbnailJobService jobService;
    private final ThumbnailerManager thumbnailerManager;
    private final ThumbnailMetrics metrics;

    @Autowired(required = false)
    private List<ThumbnailJobCompletionHandler> completionHandlers;

    @Autowired(required = false)
    private List<ThumbnailJobFailureHandler> failureHandlers;

    private final MimeTypeDetector mimeTypeDetector = new MimeTypeDetector();

    public enum ProcessResult {
        SUCCESS,
        RETRY,
        DLQ,
        SKIPPED
    }

    /**
     * Process a single job. Caller is responsible for retry/DLQ delivery (Kafka or AMQP).
     *
     * @param job        the job to process
     * @param maxRetries maximum retries before DLQ
     * @return SUCCESS, RETRY (caller should re-queue), DLQ (caller should send to DLQ), or SKIPPED
     */
    public ProcessResult process(ThumbnailJob job, int maxRetries) {
        if (job.getStatus() != ThumbnailJob.JobStatus.PENDING) {
            log.debug("Job {} already in state {}, skipping", job.getJobId(), job.getStatus());
            return ProcessResult.SKIPPED;
        }
        File inputFile = new File(job.getFilePath());
        if (!inputFile.exists() || !inputFile.isFile()) {
            metrics.recordFailure("unknown", null);
            jobService.markFailed(job.getJobId(), "File not found or not a file: " + job.getFilePath());
            notifyFailureHandlers(job, "File not found or not a file: " + job.getFilePath());
            return ProcessResult.DLQ;
        }
        String fileType = detectFileType(inputFile);
        Timer.Sample sample = metrics.startProcessingTimer();
        jobService.markProcessing(job.getJobId());
        try {
            String ext = mimeTypeDetector.getOutputExt(inputFile);
            File thumbnail = thumbnailerManager.createThumbnail(inputFile, ext);
            metrics.recordSuccess(fileType, sample);
            jobService.markCompleted(job.getJobId(), thumbnail.getAbsolutePath());
            log.info("Job {} completed, thumbnail: {}", job.getJobId(), thumbnail.getAbsolutePath());
            jobService.findById(job.getJobId()).ifPresent(this::notifyCompletionHandlers);
            return ProcessResult.SUCCESS;
        } catch (ThumbnailException | ThumbnailRuntimeException e) {
            metrics.recordFailure(fileType, sample);
            return handleFailure(job, e.getMessage(), maxRetries);
        } catch (Exception e) {
            metrics.recordFailure(fileType, sample);
            return handleFailure(job, e.getClass().getSimpleName() + ": " + e.getMessage(), maxRetries);
        }
    }

    private String detectFileType(File inputFile) {
        try {
            String mime = mimeTypeDetector.getMimeType(inputFile);
            return mime != null ? mime : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private ProcessResult handleFailure(ThumbnailJob job, String errorMessage, int maxRetries) {
        int nextRetry = job.getRetryCount() + 1;
        if (nextRetry <= maxRetries) {
            log.warn("Job {} failed (attempt {}), will retry: {}", job.getJobId(), nextRetry, errorMessage);
            jobService.markPendingForRetry(job.getJobId(), errorMessage);
            return ProcessResult.RETRY;
        }
        jobService.markFailed(job.getJobId(), errorMessage);
        notifyFailureHandlers(job, errorMessage);
        log.error("Job {} sent to DLQ after {} retries: {}", job.getJobId(), job.getRetryCount(), errorMessage);
        return ProcessResult.DLQ;
    }

    private void notifyFailureHandlers(ThumbnailJob job, String errorMessage) {
        if (failureHandlers == null || failureHandlers.isEmpty()) return;
        for (ThumbnailJobFailureHandler h : failureHandlers) {
            try {
                h.onFailed(job, errorMessage);
            } catch (Exception e) {
                log.warn(
                        "Failure handler {} failed for job {}: {}",
                        h.getClass().getSimpleName(),
                        job.getJobId(),
                        e.getMessage());
            }
        }
    }

    private void notifyCompletionHandlers(ThumbnailJob job) {
        if (completionHandlers == null || completionHandlers.isEmpty()) return;
        for (ThumbnailJobCompletionHandler h : completionHandlers) {
            try {
                h.onCompleted(job);
            } catch (IOException e) {
                log.warn(
                        "Completion handler {} failed for job {}: {}",
                        h.getClass().getSimpleName(),
                        job.getJobId(),
                        e.getMessage());
            }
        }
    }
}
