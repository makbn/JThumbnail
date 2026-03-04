package io.github.makbn.jthumbnail.core.config;

import io.github.makbn.jthumbnail.core.properties.ThumbnailServerProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableConfigurationProperties(value = {ThumbnailServerProperties.class})
public class ThumbnailServerConfiguration {

    ThumbnailServerProperties thumbnailServerProperties;

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
        log.trace("Creating {} directory", temporaryPath.toAbsolutePath());

        File temporaryDirectory = Files.createDirectory(temporaryPath).toFile();
        log.debug("Temporary upload directory created");

        return temporaryDirectory;
    }

    public int getMaxWaitingListSize() {
        return thumbnailServerProperties.maxWaitingListSize();
    }
}
