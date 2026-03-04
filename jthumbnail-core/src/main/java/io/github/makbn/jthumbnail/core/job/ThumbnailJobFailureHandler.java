package io.github.makbn.jthumbnail.core.job;

/**
 * Extension point: called when a job is marked FAILED and sent to DLQ.
 * Implementations can e.g. move the source file to a failed directory.
 */
public interface ThumbnailJobFailureHandler {

    /**
     * Called when the job has been marked FAILED. The job's filePath may still exist.
     */
    void onFailed(ThumbnailJob job, String errorMessage);
}
