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
 * Configuration properties for OpenOffice/LibreOffice. If pipeNames and ports
 * are provided, one instance per each port, and one per each pipe will be
 * started.
 *
 * @param hostname  Specifies the hostname used to connect to the
 *                  OpenOffice/LibreOffice. Defaults to 127.0.0.1
 * @param ports     Ports to access OpenOffice/LibreOffice instances. In the
 *                  managed case, this will launch instance on each port in the
 *                  managed case
 * @param pipeNames pipes to listen to, this will launch instance on each name
 *                  for managed OpenOffice and try to connect for external
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
 * @param officeHome     Path to OpenOffice/LibreOffice.
 * @param processTimeout Timeout for waiting for OpenOffice/LibreOffice process.
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.openoffice", ignoreUnknownFields = false)
public record LocalOfficeProperties(
        @NotNull ManagerType managerType,
        @NotEmpty @DefaultValue("127.0.0.1") String hostname,
        @NotNull List<@Min(1) @Max(65535) Integer> ports,
        @Nullable List<@NotNull @NotEmpty String> pipeNames,
        @Nullable @DefaultValue("false") Boolean failFast,
        File workingDir,
        @Nullable @Min(1) @DefaultValue("120000") Long connectionTimeout,
        @Nullable @Min(1) @DefaultValue("1000") Integer maxTasksPerConnection,
        @Nullable @Min(1) @DefaultValue("30000") Long taskQueueTimeout,
        @Nullable @Min(1) @DefaultValue("120000") Long taskExecutionTimeout,
        File officeHome,
        @Min(1) @DefaultValue("120000") Long processTimeout) {}
