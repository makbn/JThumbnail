package io.github.makbn.jthumbnail.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Rate limit for POST upload and POST retry. Per-client (IP or X-Forwarded-For).
 *
 * @param enabled       whether the filter is active
 * @param requestsPerMinute max requests per minute per client
 */
@ConfigurationProperties(prefix = "jthumbnailer.rate-limit", ignoreUnknownFields = true)
public record RateLimitProperties(@DefaultValue("true") boolean enabled, @DefaultValue("60") int requestsPerMinute) {}
