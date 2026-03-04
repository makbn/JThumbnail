package io.github.makbn.jthumbnail.core.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Provider registry config: ordered list of provider names for priority.
 * Unlisted providers are tried after those in the list.
 *
 * @param priority e.g. [FfmpegThumbnailer, MPEGThumbnailer]
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.providers", ignoreUnknownFields = true)
public record ProviderRegistryProperties(@DefaultValue("[]") List<String> priority) {}
