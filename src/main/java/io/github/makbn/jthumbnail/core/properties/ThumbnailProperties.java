package io.github.makbn.jthumbnail.core.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Application configuration properties
 *
 * @param thumbHeight The height of the thumbnail in pixels
 * @param thumbWidth The width of the thumbnail in pixels
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.thumbnail", ignoreUnknownFields = false)
public record ThumbnailProperties(@NotNull @Positive Integer thumbHeight, @NotNull @Positive Integer thumbWidth) {}
