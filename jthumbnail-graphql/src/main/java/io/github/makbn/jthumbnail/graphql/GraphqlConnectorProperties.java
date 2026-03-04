package io.github.makbn.jthumbnail.graphql;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the GraphQL connector.
 *
 * <p>Pattern follows other connectors:
 * {@code jthumbnailer.graphql.enabled=true} to turn it on.</p>
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.graphql", ignoreUnknownFields = false)
public record GraphqlConnectorProperties(boolean enabled) {}

