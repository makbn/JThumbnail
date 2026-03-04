package io.github.makbn.jthumbnail.amqp;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Resolves fileUrl to a local file path: file:// uses the path; http(s):// downloads to a temp file.
 */
@Component
@Slf4j
public class FileUrlResolver {

    /**
     * Resolve fileUrl to an absolute local path. For file:// returns the path; for http(s):// downloads to temp.
     *
     * @param fileUrl file URL (file:// or http(s)://)
     * @return absolute path to a local file
     * @throws IOException if resolution or download fails
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
        // Treat as local path
        Path path = Path.of(trimmed);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IOException("File does not exist or is not a regular file: " + path);
        }
        return path.toAbsolutePath().normalize().toString();
    }

    private String downloadToTempFile(String url) throws IOException {
        Path tempFile = Files.createTempFile("jthumbnail-amqp-", ".tmp");
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

    /** Returns true if the payload looks like a job ID (UUID) for retry delivery. */
    public static boolean looksLikeJobId(String payload) {
        if (payload == null || payload.isBlank()) return false;
        String s = payload.trim();
        if (s.length() != 36) return false;
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
