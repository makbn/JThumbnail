package io.github.makbn.jthumbnail.webhook;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generic webhook endpoint for CMS and other systems. Supports HMAC validation and pluggable adapters.
 */
@RestController
@RequestMapping("${jthumbnailer.webhook.path:/webhook}")
@ConditionalOnProperty(name = "jthumbnailer.webhook.enabled", havingValue = "true")
@EnableConfigurationProperties(WebhookProperties.class)
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Webhook", description = "CMS / webhook ingestion for thumbnail jobs")
public class WebhookController {

    private final WebhookHandlerService handlerService;
    private final WebhookProperties props;

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE, "application/json", "text/plain"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Webhook ingestion", description = "POST JSON payload; HMAC and replay protection optional.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Job created and enqueued"),
        @ApiResponse(responseCode = "200", description = "Replay (idempotent), no new job"),
        @ApiResponse(responseCode = "400", description = "Bad payload or no matching adapter"),
        @ApiResponse(responseCode = "401", description = "Invalid or missing signature"),
    })
    public ResponseEntity<WebhookResponse> onWebhook(
            @RequestBody(required = false) String body, HttpServletRequest request) {

        String rawBody = body != null ? body : "";
        Map<String, String> headers = headerMap(request);

        String sigHeader = headers.get(props.signatureHeader().toLowerCase());
        String signatureError = handlerService.validateSignature(rawBody, sigHeader);
        if (signatureError != null) {
            log.warn("Webhook signature validation failed: {}", signatureError);
            return ResponseEntity.status(401).body(WebhookResponse.error(signatureError));
        }

        String idem = headers.get(props.idempotencyHeader().toLowerCase());
        if (idem != null && handlerService.isReplay(idem)) {
            return ResponseEntity.ok(WebhookResponse.replayed());
        }

        WebhookJobRequest jobRequest = handlerService.mapToJobRequest(headers, rawBody);
        if (jobRequest == null) {
            return ResponseEntity.badRequest().body(WebhookResponse.error("No adapter could handle payload"));
        }

        if (jobRequest.getIdempotencyKey() != null) {
            if (handlerService.isReplay(jobRequest.getIdempotencyKey())) {
                return ResponseEntity.ok(WebhookResponse.replayed());
            }
        }

        Optional<String> jobId = handlerService.createJobFromRequest(jobRequest);
        if (jobId.isEmpty()) {
            return ResponseEntity.badRequest().body(WebhookResponse.error("Failed to create job (e.g. invalid URL)"));
        }
        return ResponseEntity.accepted().body(WebhookResponse.accepted(jobId.get()));
    }

    private static Map<String, String> headerMap(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(String::toLowerCase, request::getHeader, (a, b) -> a));
    }

    public record WebhookResponse(String jobId, boolean accepted, boolean replay, String error) {

        public static WebhookResponse accepted(String jobId) {
            return new WebhookResponse(jobId, true, false, null);
        }

        public static WebhookResponse replayed() {
            return new WebhookResponse(null, false, true, null);
        }

        public static WebhookResponse error(String message) {
            return new WebhookResponse(null, false, false, message);
        }
    }
}
