package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.exception.ThumbnailRuntimeException;
import io.github.makbn.jthumbnail.core.properties.OfficeProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OfficeManagerConfiguration {

    OfficeProperties officeProperties;

    @NonFinal
    OfficeManager officeManager;

    @Bean("officeManager")
    OfficeManager getOfficeManager() {
        if (officeManager == null) {
            Path temporaryPath;
            try {
                File workingDirPath = officeProperties.workingDir();

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

                this.officeManager = LocalOfficeManager.builder()
                        .portNumbers(officeProperties.ports().stream()
                                .mapToInt(Integer::valueOf)
                                .toArray())
                        .workingDir(temporaryDirectory)
                        .processTimeout(officeProperties.timeout())
                        .taskExecutionTimeout(officeProperties.timeout())
                        .maxTasksPerProcess(officeProperties.maxTasksPerProcess())
                        .existingProcessAction(ExistingProcessAction.CONNECT_OR_KILL)
                        .officeHome(officeProperties.officeHome())
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
}
