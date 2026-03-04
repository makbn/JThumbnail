package io.github.makbn.jthumbnail.kafka;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the thumbnail job queue (Kafka + retry + DLQ).
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.jobs", ignoreUnknownFields = false)
public record JobQueueProperties(
        @NotBlank String topic,
        @NotBlank String deadLetterTopic,
        @NotNull @Min(0) @Max(10) @DefaultValue("3") Integer maxRetries,
        @NotNull @Positive @DefaultValue("1") Integer consumerConcurrency) {}
