package io.github.makbn.jthumbnail.webhook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the CMS / webhook connector.
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.webhook", ignoreUnknownFields = false)
public record WebhookProperties(
        @NotNull @DefaultValue("false") Boolean enabled,
        @NotBlank @DefaultValue("/webhook") String path,
        /** Shared secret for HMAC signature validation; if empty, signature is not required. */
        String secret,
        /** Header name for HMAC signature (e.g. X-Webhook-Signature, X-Hub-Signature-256). */
        @NotBlank @DefaultValue("X-Webhook-Signature") String signatureHeader,
        /** Replay window in seconds: reject requests with same idempotency key seen within this window. */
        @NotNull @PositiveOrZero @DefaultValue("300") Integer replayWindowSeconds,
        /** Header name for idempotency key (e.g. X-Idempotency-Key, X-Request-Id). */
        @NotBlank @DefaultValue("X-Idempotency-Key") String idempotencyHeader) {}
