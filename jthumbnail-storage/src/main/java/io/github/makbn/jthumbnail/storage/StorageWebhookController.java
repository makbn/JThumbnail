package io.github.makbn.jthumbnail.storage;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP endpoint for S3-compatible event webhooks (MinIO or SNS HTTP subscription).
 * POST body is the raw event JSON.
 */
@RestController
@RequestMapping("${jthumbnailer.storage.webhook-path:/storage/events}")
@ConditionalOnProperty(name = "jthumbnailer.storage.enabled", havingValue = "true")
@Slf4j
public class StorageWebhookController {

    private final S3ThumbnailTriggerService triggerService;

    public StorageWebhookController(S3ThumbnailTriggerService triggerService) {
        this.triggerService = triggerService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json", "text/plain"})
    public ResponseEntity<WebhookResponse> onEvent(@RequestBody(required = false) String body) {
        if (body == null || body.isBlank()) {
            return ResponseEntity.badRequest().body(new WebhookResponse(0, "Empty body"));
        }
        try {
            int enqueued = triggerService.processEventPayload(body);
            return ResponseEntity.ok(new WebhookResponse(enqueued, null));
        } catch (Exception e) {
            log.warn("Webhook processing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new WebhookResponse(0, e.getMessage()));
        }
    }

    public record WebhookResponse(int jobsEnqueued, String error) {}
}
