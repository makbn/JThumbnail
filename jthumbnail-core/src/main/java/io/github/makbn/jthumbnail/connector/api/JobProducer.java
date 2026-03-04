package io.github.makbn.jthumbnail.connector.api;

/**
 * Optional contract for sending thumbnail job IDs to a queue (e.g. Kafka). When no implementation
 * is present, jobs are still created but not enqueued. Implementations are provided by connector
 * modules (e.g. jthumbnail-kafka).
 */
public interface JobProducer {

    /**
     * Send a job ID to the main job topic for processing.
     *
     * @param jobId the job id
     */
    void sendJob(String jobId);

    /**
     * Send a job ID to the dead-letter topic after max retries.
     *
     * @param jobId the job id
     */
    void sendToDeadLetter(String jobId);
}
