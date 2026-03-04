package io.github.makbn.jthumbnail.webhook;

import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;
import io.github.makbn.jthumbnail.core.metrics.ThumbnailMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Transforms webhook payload to ThumbnailJob and enqueues. Used by {@link WebhookController}.
 */
@Service
@ConditionalOnProperty(name = "jthumbnailer.webhook.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class WebhookHandlerService {

    private final WebhookProperties props;
    private final List<WebhookAdapter> adapters;
    private final WebhookUrlResolver urlResolver;
    private final ThumbnailJobService jobService;
    private final ThumbnailJobSubmitter jobSubmitter;
    private final ThumbnailMetrics metrics;
    private final WebhookSignatureValidator signatureValidator;
    private final ReplayProtection replayProtection;

    /**
     * Validate signature if secret is configured.
     *
     * @return null if valid, error message if invalid
     */
    public String validateSignature(String rawBody, String signatureHeaderValue) {
        if (props.secret() == null || props.secret().isBlank()) {
            return null;
        }
        if (signatureHeaderValue == null || signatureHeaderValue.isBlank()) {
            return "Missing signature header: " + props.signatureHeader();
        }
        if (!signatureValidator.validate(rawBody, signatureHeaderValue, props.secret())) {
            return "Invalid signature";
        }
        return null;
    }

    /**
     * Check replay protection; returns true if this is a replay (should respond 200 without processing).
     */
    public boolean isReplay(String idempotencyKey) {
        return replayProtection.isReplay(idempotencyKey, props.replayWindowSeconds());
    }

    /**
     * Map payload to job request using first matching adapter.
     */
    public WebhookJobRequest mapToJobRequest(Map<String, String> headers, String rawBody) {
        for (WebhookAdapter adapter : adapters) {
            if (adapter.canHandle(headers, rawBody)) {
                WebhookJobRequest req = adapter.toJobRequest(headers, rawBody);
                if (req != null) {
                    log.debug("Webhook mapped by adapter {} to fileUrl={}", adapter.getName(), req.getFileUrl());
                    return req;
                }
            }
        }
        return null;
    }

    /**
     * Resolve fileUrl to local path, create job, and optionally send to queue.
     *
     * @return created job id, or empty if failed
     */
    public Optional<String> createJobFromRequest(WebhookJobRequest request) {
        if (request == null
                || request.getFileUrl() == null
                || request.getFileUrl().isBlank()) {
            return Optional.empty();
        }
        try {
            String localPath = urlResolver.resolveToLocalPath(request.getFileUrl());
            String jobId = jobSubmitter.submit(localPath);
            log.info("Webhook job created: {} for sourceId={}", jobId, request.getSourceId());
            return Optional.of(jobId);
        } catch (Exception e) {
            log.warn("Webhook job creation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
