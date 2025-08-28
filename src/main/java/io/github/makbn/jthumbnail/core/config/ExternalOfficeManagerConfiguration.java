package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.exception.ThumbnailRuntimeException;
import io.github.makbn.jthumbnail.core.properties.ExternalOfficeProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnProperty(value = "jthumbnailer.openoffice.manager_type", havingValue = "external")
@EnableConfigurationProperties(value = {ExternalOfficeProperties.class})
@Validated
public class ExternalOfficeManagerConfiguration {

    ExternalOfficeProperties externalOfficeProperties;

    @NonFinal
    OfficeManager officeManager;

    @Bean("officeManager")
    OfficeManager getOfficeManager() {
        if (officeManager == null) {
            Path temporaryPath;
            try {
                File workingDirPath = externalOfficeProperties.workingDir();

                if (workingDirPath == null) {
                    // We use the OS temporary directory
                    workingDirPath = FileUtils.getTempDirectory();
                }

                if (!workingDirPath.exists()) {
                    log.info("Working directory doesn't exist, creating it");
                    workingDirPath =
                            Files.createDirectories(workingDirPath.toPath()).toFile();
                }

                if (!workingDirPath.isDirectory()) {
                    log.error("Provided path for working directory is not a directory");
                    throw new IOException("Provided path for working directory is not a directory");
                }

                // Create the temporary directory
                log.debug("Creating temporary directory inside working directory");
                temporaryPath = workingDirPath.toPath().resolve(String.valueOf(System.currentTimeMillis()));
                log.trace("Creating {} directory", temporaryPath.toAbsolutePath());

                File temporaryDirectory = Files.createDirectory(temporaryPath).toFile();
                log.debug("Temporary working directory created");

                this.officeManager = ExternalOfficeManager.builder()
                        .hostName(externalOfficeProperties.hostname())
                        .portNumbers(externalOfficeProperties.ports().stream()
                                .mapToInt(Integer::valueOf)
                                .toArray())
                        .pipeNames(Optional.ofNullable(externalOfficeProperties.pipeNames())
                                .orElse(Collections.emptyList())
                                .toArray(String[]::new))
                        .websocketUrls(Optional.ofNullable(externalOfficeProperties.websocketUrls())
                                .orElse(Collections.emptyList())
                                .toArray(String[]::new))
                        .connectOnStart(externalOfficeProperties.connectOnStart())
                        .connectFailFast(externalOfficeProperties.failFast())
                        .connectTimeout(externalOfficeProperties.connectionTimeout())
                        .connectRetryInterval(externalOfficeProperties.connectRetryInterval())
                        .maxTasksPerConnection(externalOfficeProperties.maxTasksPerConnection())
                        .taskQueueTimeout(externalOfficeProperties.taskQueueTimeout())
                        .taskExecutionTimeout(externalOfficeProperties.taskExecutionTimeout())
                        .workingDir(temporaryDirectory)
                        .install()
                        .build();
                officeManager.start();
            } catch (IOException e) {
                log.error("Failed to create working directory", e);
                throw new ThumbnailRuntimeException(e);
            } catch (OfficeException e) {
                log.error("Failed to start Office manager", e);
                throw new ThumbnailRuntimeException(e);
            }
        }
        return officeManager;
    }

    @Bean("converter")
    DocumentConverter getConverter() {
        return LocalConverter.builder().officeManager(getOfficeManager()).build();
    }
}
