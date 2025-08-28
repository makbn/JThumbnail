package io.github.makbn.jthumbnail.core.properties;

import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.File;

/**
 * Configuration properties for API Thumbnail server
 *
 * @param uploadDirectory uploadDirectory
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.server", ignoreUnknownFields = false)
public record ThumbnailServerProperties(@NotNull File uploadDirectory, @NotNull int maxWaitingListSize) {}
