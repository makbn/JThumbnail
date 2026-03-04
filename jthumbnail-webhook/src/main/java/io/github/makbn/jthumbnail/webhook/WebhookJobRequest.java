package io.github.makbn.jthumbnail.webhook;

import lombok.Builder;
import lombok.Value;

/**
 * Result of mapping an incoming webhook payload to a thumbnail job request.
 */
@Value
@Builder
public class WebhookJobRequest {

    /** URL or path of the file to thumbnail (required). */
    String fileUrl;

    /** Optional idempotency key for replay protection. */
    String idempotencyKey;

    /** Optional source identifier (e.g. CMS entity id) for logging. */
    String sourceId;
}
