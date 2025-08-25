package io.github.makbn.jthumbnail.core.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jthumbnailer.async", ignoreUnknownFields = false)
public record AsyncProperties(
        @NotNull @DefaultValue("10") @Min(1) Integer corePoolSize,
        @NotNull @DefaultValue("32") @Min(1) Integer maxPoolSize) {}
