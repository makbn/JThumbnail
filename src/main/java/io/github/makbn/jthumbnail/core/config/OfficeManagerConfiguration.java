package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.exception.ThumbnailerRuntimeException;
import io.github.makbn.jthumbnail.core.properties.OfficeProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.ExistingProcessAction;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class OfficeManagerConfiguration {

    private final OfficeProperties officeProperties;

    private OfficeManager officeManager = null;

    @Bean("officeManager")
    OfficeManager getOfficeManager() throws IOException {
        if (officeManager == null) {
            Path temporaryPath = null;

            try {
                File workingDirPath = officeProperties.workingDir();

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
                log.trace("Creating " + temporaryPath.toAbsolutePath().toString() + " directory");

                temporaryPath = Files.createDirectory(temporaryPath);
                log.debug("Temporary working directory created");

            } catch (IOException ex) {
                log.error("Working directory creation failed", ex);
                throw ex;
            }

            this.officeManager = LocalOfficeManager.builder()
                    .portNumbers(officeProperties.ports().stream()
                            .mapToInt(Integer::valueOf)
                            .toArray())
                    .workingDir(temporaryPath.toFile())
                    .processTimeout(officeProperties.timeout())
                    .taskExecutionTimeout(officeProperties.timeout())
                    .maxTasksPerProcess(officeProperties.maxTasksPerProcess())
                    .existingProcessAction(ExistingProcessAction.CONNECT_OR_KILL)
                    .officeHome(officeProperties.officeHome())
                    .install()
                    .build();
            try {
                officeManager.start();
            } catch (OfficeException e) {
                throw new ThumbnailerRuntimeException(e);
            }
        }
        return officeManager;
    }
}
