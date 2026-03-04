package io.github.makbn.jthumbnail.watcher;

import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import io.github.makbn.jthumbnail.core.metrics.ThumbnailMetrics;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Watches configured directories with NIO WatchService; debounces and checks
 * file size stability before enqueueing thumbnail jobs to avoid partial uploads.
 */
@Component
@ConditionalOnProperty(name = "jthumbnailer.watcher.enabled", havingValue = "true")
@Slf4j
public class FilesystemWatcherConnector {

    private final WatcherProperties props;
    private final ThumbnailJobSubmitter jobSubmitter;
    private final ThumbnailMetrics metrics;

    private WatchService watchService;
    private final Map<Path, PendingFile> pending = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "watcher-poll");
        t.setDaemon(false);
        return t;
    });
    private volatile boolean running;

    public FilesystemWatcherConnector(
            WatcherProperties props, ThumbnailJobSubmitter jobSubmitter, ThumbnailMetrics metrics) {
        this.props = props;
        this.jobSubmitter = jobSubmitter;
        this.metrics = metrics;
    }

    private static final class PendingFile {
        long lastEventTime;
        long lastSize;
        long lastSizeChangeTime;

        PendingFile(long now, long size) {
            this.lastEventTime = now;
            this.lastSize = size;
            this.lastSizeChangeTime = now;
        }
    }

    @PostConstruct
    void start() throws IOException {
        if (props.directories() == null || props.directories().isEmpty()) {
            log.warn("Watcher enabled but no directories configured");
            return;
        }
        watchService = FileSystems.getDefault().newWatchService();
        for (String dir : props.directories()) {
            Path root = Paths.get(dir).toAbsolutePath().normalize();
            if (!Files.isDirectory(root)) {
                log.warn("Watcher directory does not exist or is not a directory: {}", root);
                continue;
            }
            registerRecursive(root);
        }
        running = true;
        scheduler.scheduleWithFixedDelay(
                this::pollWatchService, 0, Math.max(100, props.pollIntervalMs()), TimeUnit.MILLISECONDS);
        scheduler.scheduleWithFixedDelay(
                this::processStableFiles,
                props.debounceMs() + props.stabilizationMs(),
                Math.max(500, props.pollIntervalMs()),
                TimeUnit.MILLISECONDS);
        log.info(
                "Filesystem watcher started for {} directory(ies)",
                props.directories().size());
    }

    @PreDestroy
    void stop() {
        running = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.debug("WatchService close: {}", e.getMessage());
            }
        }
    }

    private void registerRecursive(Path dir) throws IOException {
        dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        String processedName = props.processedDir() != null
                ? Paths.get(props.processedDir()).getFileName().toString()
                : "processed";
        String failedName = props.failedDir() != null
                ? Paths.get(props.failedDir()).getFileName().toString()
                : "failed";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                if (!Files.isDirectory(child)) continue;
                String name = child.getFileName().toString();
                if (name.startsWith(".") || name.equals(processedName) || name.equals(failedName)) continue;
                registerRecursive(child);
            }
        }
    }

    private void pollWatchService() {
        if (!running || watchService == null) return;
        WatchKey key = watchService.poll();
        if (key == null) return;
        Path watchRoot = (Path) key.watchable();
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();
            if (kind == StandardWatchEventKinds.OVERFLOW) continue;
            Path name = (Path) event.context();
            Path full = watchRoot.resolve(name).toAbsolutePath().normalize();
            if (Files.isDirectory(full)) {
                String subName = full.getFileName().toString();
                String processedName = props.processedDir() != null
                        ? Paths.get(props.processedDir()).getFileName().toString()
                        : "processed";
                String failedName = props.failedDir() != null
                        ? Paths.get(props.failedDir()).getFileName().toString()
                        : "failed";
                if (!subName.equals(processedName) && !subName.equals(failedName)) {
                    try {
                        registerRecursive(full);
                    } catch (IOException e) {
                        log.warn("Could not register new directory {}: {}", full, e.getMessage());
                    }
                }
                continue;
            }
            if (!Files.isRegularFile(full)) continue;
            long now = System.currentTimeMillis();
            long size = full.toFile().length();
            pending.merge(full, new PendingFile(now, size), (old, ign) -> {
                old.lastEventTime = now;
                if (old.lastSize != size) {
                    old.lastSize = size;
                    old.lastSizeChangeTime = now;
                }
                return old;
            });
        }
        key.reset();
    }

    private void processStableFiles() {
        if (!running) return;
        long now = System.currentTimeMillis();
        long debounce = props.debounceMs();
        long stabil = props.stabilizationMs();
        List<Path> toProcess = pending.entrySet().stream()
                .filter(e -> {
                    PendingFile p = e.getValue();
                    if (now - p.lastEventTime < debounce) return false;
                    if (now - p.lastSizeChangeTime < stabil) return false;
                    return true;
                })
                .map(Map.Entry::getKey)
                .toList();
        for (Path path : toProcess) {
            pending.remove(path);
            if (!Files.isRegularFile(path)) continue;
            enqueue(path);
        }
    }

    private void enqueue(Path path) {
        String absolutePath = path.toAbsolutePath().toString();
        Path watchRoot = findWatchRoot(path);
        if (watchRoot == null) return;
        Path processedDir = resolveDir(watchRoot, props.processedDir());
        Path failedDir = resolveDir(watchRoot, props.failedDir());
        String processedPath =
                processedDir != null ? processedDir.toAbsolutePath().toString() : null;
        String failedPath = failedDir != null ? failedDir.toAbsolutePath().toString() : null;
        String jobId = jobSubmitter.submitForWatcher(absolutePath, processedPath, failedPath);
        log.debug("Enqueued watcher job {} for {}", jobId, path);
    }

    private Path findWatchRoot(Path path) {
        for (String dir : props.directories()) {
            Path root = Paths.get(dir).toAbsolutePath().normalize();
            if (path.startsWith(root)) return root;
        }
        return null;
    }

    private Path resolveDir(Path watchRoot, String subDir) {
        if (subDir == null || subDir.isBlank()) return null;
        Path p = Paths.get(subDir);
        if (p.isAbsolute()) return p.normalize();
        return watchRoot.resolve(subDir).normalize();
    }
}
