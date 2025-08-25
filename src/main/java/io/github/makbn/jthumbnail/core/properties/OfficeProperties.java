package io.github.makbn.jthumbnail.core.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for OpenOffice/LibreOffice
 *
 * @param officeHome path to OpenOffice/LibreOffice
 * @param workingDir path to a working directory
 * @param ports Ports to listen to, this will launch instance on each port
 * @param maxTasksPerProcess Maximum number of tasks per process
 * @param timeout Timeout for waiting for OpenOffice/LibreOffice
 * @param tmpDir temporary directory
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.openoffice", ignoreUnknownFields = false)
public record OfficeProperties(
        File officeHome,
        File workingDir,
        @Name("port") @NotNull List<@Min(1) @Max(65535) Integer> ports,
        @NotNull @Min(1) Integer maxTasksPerProcess,
        @NotNull @Min(1) Long timeout,
        @NotNull File tmpDir) {}
