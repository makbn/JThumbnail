package io.github.makbn.jthumbnail.core.job;

import java.io.IOException;

/**
 * Extension point: called after a thumbnail job is completed successfully.
 * Implementations can e.g. upload the thumbnail to S3 or notify external systems.
 */
public interface ThumbnailJobCompletionHandler {

    /**
     * Called when a job has been marked COMPLETED and the thumbnail file exists at
     * {@code job.getThumbnailPath()}. Implementations should not throw; log and handle errors.
     */
    void onCompleted(ThumbnailJob job) throws IOException;
}
