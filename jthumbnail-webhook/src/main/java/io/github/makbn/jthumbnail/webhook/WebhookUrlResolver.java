package io.github.makbn.jthumbnail.webhook;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves a file URL to a local path; downloads http(s) URLs to a temp file.
 */
@Component
@Slf4j
public class WebhookUrlResolver {

    /**
     * Resolve fileUrl to an absolute local path. For http(s):// downloads to temp.
     *
     * @param fileUrl file URL (http(s):// or file://)
     * @return absolute path to a local file
     */
    public String resolveToLocalPath(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IOException("fileUrl is blank");
        }
        String trimmed = fileUrl.trim();
        if (trimmed.startsWith("file:/")) {
            URI uri = URI.create(trimmed);
            Path path = Path.of(uri.getPath());
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                throw new IOException("File does not exist or is not a regular file: " + path);
            }
            return path.toAbsolutePath().normalize().toString();
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return downloadToTempFile(trimmed);
        }
        Path path = Path.of(trimmed);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IOException("File does not exist or is not a regular file: " + path);
        }
        return path.toAbsolutePath().normalize().toString();
    }

    private String downloadToTempFile(String url) throws IOException {
        Path tempFile = Files.createTempFile("jthumbnail-webhook-", ".tmp");
        try {
            URI uri = URI.create(url);
            try (InputStream in = uri.toURL().openStream()) {
                Files.copy(in, tempFile);
            }
            log.debug("Downloaded {} to {}", url, tempFile);
            return tempFile.toAbsolutePath().normalize().toString();
        } catch (Exception e) {
            Files.deleteIfExists(tempFile);
            throw new IOException("Failed to download " + url + ": " + e.getMessage(), e);
        }
    }
}
