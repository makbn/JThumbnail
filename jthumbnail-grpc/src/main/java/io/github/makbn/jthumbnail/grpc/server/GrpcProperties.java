package io.github.makbn.jthumbnail.grpc.server;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration for the gRPC thumbnail service (port, TLS).
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.grpc", ignoreUnknownFields = false)
public record GrpcProperties(
        @NotNull @DefaultValue("false") Boolean enabled,
        @NotNull @Min(1) @Max(65535) @DefaultValue("9090") Integer port,
        @NotNull @DefaultValue("false") Boolean useTls,
        /** Path to PEM-encoded certificate chain file (optional when useTls is true). */
        String certChainFile,
        /** Path to PEM-encoded private key file (optional when useTls is true). */
        String privateKeyFile) {}
