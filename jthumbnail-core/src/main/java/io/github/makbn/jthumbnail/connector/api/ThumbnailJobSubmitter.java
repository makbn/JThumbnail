package io.github.makbn.jthumbnail.connector.api;

/**
 * Public API for submitting thumbnail jobs from any connector (REST, Kafka, AMQP, gRPC, webhook,
 * filesystem watcher, S3, etc.).
 * <p>
 * All connectors that <em>trigger</em> thumbnail generation should use this interface so that:
 * <ul>
 *   <li>Job creation and optional queue dispatch are consistent</li>
 *   <li>Metrics and lifecycle are centralized</li>
 *   <li>Third-party connectors can depend only on this API</li>
 * </ul>
 * See {@link io.github.makbn.jthumbnail.connector.api} package documentation and the project
 * CONNECTOR_SPECIFICATION for the full contract.
 */
public interface ThumbnailJobSubmitter {

    /**
     * Submit a thumbnail job for a local file. Creates the job and, if a job queue is configured,
     * enqueues it for processing.
     *
     * @param localFilePath absolute path to the source file
     * @return the created job id (never null)
     */
    String submit(String localFilePath);

    /**
     * Submit a job from the filesystem watcher connector. On completion or failure, the source
     * file can be moved to processed/failed directories.
     *
     * @param localFilePath       absolute path to the source file
     * @param moveToProcessedDir  directory to move file to on success (optional)
     * @param moveToFailedDir     directory to move file to on failure (optional)
     * @return the created job id (never null)
     */
    String submitForWatcher(String localFilePath, String moveToProcessedDir, String moveToFailedDir);

    /**
     * Submit a job from S3 (or S3-compatible) storage. The thumbnail will be uploaded back to the
     * configured output location.
     *
     * @param localFilePath  absolute path to the downloaded file
     * @param sourceBucket   source bucket
     * @param sourceKey      source object key
     * @param outputBucket   bucket for thumbnail output
     * @param outputKey      key for thumbnail output
     * @return the created job id (never null)
     */
    String submitForS3(
            String localFilePath, String sourceBucket, String sourceKey, String outputBucket, String outputKey);
}
