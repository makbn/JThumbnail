package io.github.makbn.jthumbnail.watcher;

import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobCompletionHandler;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

/**
 * Moves the source file to the configured processed directory when a watcher job completes.
 */
@Component
@ConditionalOnProperty(name = "jthumbnailer.watcher.enabled", havingValue = "true")
@Slf4j
public class WatcherCompletionHandler implements ThumbnailJobCompletionHandler {

    @Override
    public void onCompleted(ThumbnailJob job) throws IOException {
        if (!job.isWatcherJob() || job.getMoveToProcessedDir() == null) return;
        Path source = Path.of(job.getFilePath());
        if (!Files.isRegularFile(source)) return;
        Path targetDir = Paths.get(job.getMoveToProcessedDir());
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(source.getFileName());
        if (target.equals(source)) return;
        moveOrCopy(source, target);
        log.debug("Moved {} to processed {}", source, target);
    }

    private static void moveOrCopy(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(source);
        }
    }
}
