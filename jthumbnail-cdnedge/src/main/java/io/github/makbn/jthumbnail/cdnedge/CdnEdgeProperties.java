package io.github.makbn.jthumbnail.cdnedge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Configuration for CDN Edge / URL-based connector.
 *
 * <p>Pattern follows other connectors:
 * {@code jthumbnailer.cdnedge.enabled=true} to turn it on, plus URL/timeout
 * and whitelist configuration.</p>
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.cdnedge", ignoreUnknownFields = false)
public record CdnEdgeProperties(
        /** Enable/disable the connector. */
        boolean enabled,

        /**
         * Optional list of allowed domains/hosts. When non-empty, only URLs
         * whose host is in this list are accepted.
         */
        List<@NotBlank String> allowedHosts,

        /**
         * Optional list of allowed file extensions (e.g. "jpg", "png", "pdf").
         * When non-empty, only those extensions are processed.
         */
        List<@NotBlank String> allowedExtensions,

        /**
         * Maximum size in bytes for the downloaded file. Larger responses are
         * rejected. This protects the service from large downloads.
         */
        @NotNull @Positive Long maxBytes,

        /**
         * HTTP client timeout for downloading the source URL. Expressed in
         * ISO-8601 duration format (e.g. "PT30S").
         */
        @NotNull Duration downloadTimeout) {

    public static final long DEFAULT_MAX_BYTES = 50L * 1024L * 1024L;

    /** Returns a properties instance with default values (connector disabled). */
    public static CdnEdgeProperties withDefaults() {
        return new CdnEdgeProperties(false, List.of(), List.of(), DEFAULT_MAX_BYTES, Duration.ofSeconds(30));
    }
}
