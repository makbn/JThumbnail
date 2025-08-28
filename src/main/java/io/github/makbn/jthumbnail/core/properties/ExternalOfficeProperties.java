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
 * @param hostname  Specifies the hostname used to connect to the
 *                  OpenOffice/LibreOffice. Defaults to 127.0.0.1
 * @param ports     Ports to access OpenOffice/LibreOffice instances. In the
 *                  managed case, this will launch instance on each port in the
 *                  managed case
 * @param pipeNames pipes to listen to, this will launch instance on each name
 *                  for managed OpenOffice and try to connect for external
 * @param websocketUrls        Websocket URLs used by the external
 *                             OpenOffice/LibreOffice.
 * @param connectOnStart       Whether we should try to connect when the manager
 *                             starts or wait for the first conversion. Defaults
 *                             to true
 * @param connectRetryInterval Time between two connections attempts
 *
 * @param failFast  should fail fast when starting
 * @param workingDir            OpenOffice/LibreOffice Specifies the directory
 *                              where temporary files and directories are
 *                              created.
 * @param connectionTimeout     Timeout for waiting for OpenOffice/LibreOffice
 *                              connection. Defaults to 120000
 * @param maxTasksPerConnection Maximum number of tasks per process/connection.
 *                              Defaults to 1000
 * @param taskQueueTimeout      Max duration a conversion will wait in queue.
 *                              Defaults to 30000
 * @param taskExecutionTimeout  Maximum duration a conversion should last.
 *                              Defaults to 120000
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
