package io.github.makbn.jthumbnail.core.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jthumbnailer.async", ignoreUnknownFields = false)
public record AsyncProperties(
        @NotNull @DefaultValue("10") @Positive Integer corePoolSize,
        @NotNull @DefaultValue("32") @Positive Integer maxPoolSize) {}
