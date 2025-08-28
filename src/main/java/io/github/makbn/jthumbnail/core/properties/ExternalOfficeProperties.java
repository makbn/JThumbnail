package io.github.makbn.jthumbnail.core.properties;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.util.List;

/**
 * Configuration properties for external OpenOffice/LibreOffice. If pipeNames
 * and ports are provided, one instance per each port, and one per each pipe
 * must be started.
 *
 * see <a href=
 * "https://jodconverter.github.io/jodconverter/latest/configuration/external-manager/">JODConverter
 * documentation</a>
 *
 * @param websocketUrls        Websocket URLs used by the external
 *                             OpenOffice/LibreOffice.
 * @param connectOnStart       Whether we should try to connect when the manager
 *                             starts or wait for the first conversion. Defaults
 *                             to true
 * @param connectRetryInterval Time between two connections attempts
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.openoffice", ignoreUnknownFields = false)
public record ExternalOfficeProperties(
        @NotNull ManagerType managerType,
        @NotEmpty @DefaultValue("127.0.0.1") String hostname,
        @NotNull List<@Min(1) @Max(65535) Integer> ports,
        @Nullable List<@NotNull @NotEmpty String> pipeNames,
        @Nullable List<@NotEmpty String> websocketUrls,
        @Nullable @Min(1) @DefaultValue("120000") Long connectionTimeout,
        @Nullable @Min(1) @DefaultValue("1000") Integer maxTasksPerConnection,
        @Nullable @Min(1) @DefaultValue("30000") Long taskQueueTimeout,
        @Nullable @Min(1) @DefaultValue("120000") Long taskExecutionTimeout,
        @Nullable @DefaultValue("false") Boolean failFast,
        @Nullable @DefaultValue("true") Boolean connectOnStart,
        @Nullable @Min(1) @DefaultValue("250") Long connectRetryInterval,
        File workingDir) {}
