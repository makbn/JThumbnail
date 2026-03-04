/**
 * Public connector API for JThumbnail.
 * <p>
 * <b>Contract for connector developers</b>
 * <ol>
 *   <li><b>Trigger:</b> Your connector receives input (HTTP, message queue, webhook, etc.) and
 *       must produce a <em>local file path</em> (or a URL that you resolve to a local path).
 *   <li><b>Submit:</b> Call {@link io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter}
 *       with the appropriate method ({@code submit}, {@code submitForWatcher}, or
 *       {@code submitForS3}) to create a job and optionally enqueue it.
 *   <li><b>Processing:</b> Jobs are processed by the core pipeline (Kafka consumer, AMQP
 *       consumer, or in-process). You do not implement processing; the runtime uses
 *       {@link io.github.makbn.jthumbnail.core.job.ThumbnailJobProcessor} and optional
 *       {@link io.github.makbn.jthumbnail.connector.api.JobProducer}.
 * </ol>
 * <p>
 * Configuration follows the pattern {@code jthumbnailer.<connector>.enabled} and
 * connector-specific properties. See the project <b>CONNECTOR_SPECIFICATION.md</b> for the full
 * specification and list of built-in connectors.
 */
package io.github.makbn.jthumbnail.connector.api;
