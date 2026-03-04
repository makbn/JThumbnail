package io.github.makbn.jthumbnail.watcher;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Filesystem watcher connector config: watched directories, debounce,
 * file size stabilization, retry policy, and processed/failed directories.
 *
 * @param enabled           whether the watcher is active
 * @param directories       list of paths to watch (recursive)
 * @param pollIntervalMs    how often to check for stable files after events
 * @param debounceMs        wait this long after last event before considering file
 * @param stabilizationMs   require file size unchanged for this long (partial uploads)
 * @param maxRetries        retries for thumbnail generation (aligned with job queue)
 * @param processedDir      move source file here on success (relative to watch dir or absolute)
 * @param failedDir         move source file here on failure
 */
@ConfigurationProperties(prefix = "jthumbnailer.watcher", ignoreUnknownFields = true)
public record WatcherProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("{}") List<String> directories,
        @DefaultValue("1000") long pollIntervalMs,
        @DefaultValue("2000") long debounceMs,
        @DefaultValue("500") long stabilizationMs,
        @DefaultValue("3") int maxRetries,
        @DefaultValue("processed") String processedDir,
        @DefaultValue("failed") String failedDir) {}
