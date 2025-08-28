package io.github.makbn.jthumbnail.core.properties;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.net.URI;

/**
 * Configuration properties for external OpenOffice/LibreOffice. If pipeNames
 * and ports are provided, one instance per each port, and one per each pipe
 * must be started.
 *
 * see <a href=
 * "https://jodconverter.github.io/jodconverter/latest/configuration/external-manager/">JODConverter
 * documentation</a>
 *
 * @param urlConnection         Specifies the URL of the REST API enabled Libre/openOffice remote server.
 *
 * @param poolSize              Setting this property controls how many conversions can be done concurrently.
 *                              Defaults to 1
 * @param connectionTimeout     Timeout for waiting for API
 *                              connection. Defaults to 120000
 * @param socketTimeout         Socket timeout for waiting for API
 *                              connection. Defaults to 120000
 * @param maxTasksPerConnection Maximum number of tasks per connection before
 *                              reconnecting. Defaults to 1000
 * @param taskQueueTimeout      Max duration a conversion will wait in queue.
 *                              Defaults to 30000
 * @param taskExecutionTimeout  Maximum duration a conversion should last.
 *                              Defaults to 120000
 * @param workingDir            Specifies the directory where temporary files
 *                              and directories are created.
 */
@Validated
@ConfigurationProperties(prefix = "jthumbnailer.openoffice", ignoreUnknownFields = false)
public record RemoteOfficeProperties(
        @NotNull ManagerType managerType,
        File workingDir,
        @Nullable @Min(1) @DefaultValue("1") Integer poolSize,
        @NotNull URI urlConnection,
        @Nullable @Min(1) @DefaultValue("120000") Long connectionTimeout,
        @Nullable @Min(1) @DefaultValue("120000") Long socketTimeout,
        @Nullable @Min(1) @DefaultValue("1000") Integer maxTasksPerConnection,
        @Nullable @Min(1) @DefaultValue("30000") Long taskQueueTimeout,
        @Nullable @Min(1) @DefaultValue("120000") Long taskExecutionTimeout) {}
