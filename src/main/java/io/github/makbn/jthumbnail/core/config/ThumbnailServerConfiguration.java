package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.properties.ThumbnailServerProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ThumbnailServerConfiguration {

    private final ThumbnailServerProperties thumbnailServerProperties;

    public File getUploadDirectory() throws IOException {
        File uploadDirectory = thumbnailServerProperties.uploadDirectory();

        if (!uploadDirectory.exists()) {
            log.info("Upload directory doesn't exist, creating it");
            uploadDirectory = Files.createDirectories(uploadDirectory.toPath()).toFile();
        }

        if (!uploadDirectory.isDirectory()) {
            log.error("Provided path for upload directory is not a directory");
            throw new IOException("Provided path for upload directory is not a directory");
        }

        // Create the temporary directory
        log.debug("Creating temporary directory inside upload directory");
        var temporaryPath = uploadDirectory.toPath().resolve(String.valueOf(System.currentTimeMillis()));
        log.trace("Creating " + temporaryPath.toAbsolutePath().toString() + " directory");

        temporaryPath = Files.createDirectory(temporaryPath);
        log.debug("Temporary upload directory created");

        return temporaryPath.toFile();
    }
}
