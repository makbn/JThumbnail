package io.github.makbn.jthumbnail.webhook;

import java.util.Map;

/**
 * Pluggable adapter that maps a webhook payload to a {@link WebhookJobRequest}.
 * Adapters are tried in order; the first that {@link #canHandle} returns true is used.
 */
public interface WebhookAdapter {

    /** Display name for logging (e.g. "wordpress", "generic-json"). */
    String getName();

    /**
     * Whether this adapter can handle the given request (e.g. by content-type or payload shape).
     *
     * @param headers request headers (lowercase keys)
     * @param rawBody raw request body
     * @return true if this adapter should process the payload
     */
    boolean canHandle(Map<String, String> headers, String rawBody);

    /**
     * Map the payload to a job request. Called only if {@link #canHandle} returned true.
     *
     * @param headers request headers (lowercase keys)
     * @param rawBody raw request body
     * @return job request, or null if mapping failed (e.g. invalid payload)
     */
    WebhookJobRequest toJobRequest(Map<String, String> headers, String rawBody);
}
