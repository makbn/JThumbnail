package io.github.makbn.jthumbnail.amqp;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the AMQP thumbnail connector (RabbitMQ, Azure Service Bus compatible).
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.amqp", ignoreUnknownFields = false)
public record AmqpProperties(
        @NotNull @DefaultValue("false") Boolean enabled,
        @NotBlank @DefaultValue("jthumbnail.exchange") String exchange,
        @NotBlank @DefaultValue("thumbnail-jobs") String queue,
        @NotBlank @DefaultValue("thumbnail.request") String routingKey,
        @NotBlank @DefaultValue("thumbnail-jobs.dlq") String deadLetterQueue,
        @NotBlank @DefaultValue("jthumbnail.dlx") String deadLetterExchange,
        @NotNull @Min(0) @Max(10) @DefaultValue("3") Integer maxRetries,
        @NotNull @Positive @DefaultValue("1") Integer consumerConcurrency,
        @NotNull @Positive @DefaultValue("30000") Long retryDelayMs) {}
