package io.github.makbn.jthumbnail.core.properties;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.util.List;

/**
 * Configuration properties for OpenOffice/LibreOffice.
 * If pipeNames and ports are provided, one instance per each port, and one per each pipe will
 * be started.
 *
 * @param officeHome         Path to OpenOffice/LibreOffice.
 * @param workingDir         OpenOffice/LibreOffice Specifies the directory where temporary files and directories are created.
 * @param ports              Ports to listen to, this will launch instance on each port.
 * @param pipeNames              pipes to listen to, this will launch instance on each name.
 * @param maxTasksPerProcess Maximum number of tasks per process.
 * @param timeout            Timeout for waiting for OpenOffice/LibreOffice.
 * @param tmpDir             Temporary directory.
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.openoffice", ignoreUnknownFields = false)
public record OfficeProperties(
        File officeHome,
        File workingDir,
        @NotNull List<@Min(1) @Max(65535) Integer> ports,
        @Nullable List<String> pipeNames,
        @NotNull @Min(1) Integer maxTasksPerProcess,
        @NotNull @Min(1) Long timeout,
        @NotNull File tmpDir) {}
