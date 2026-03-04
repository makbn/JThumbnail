package io.github.makbn.jthumbnail.watcher;

import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobFailureHandler;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

/**
 * Moves the source file to the configured failed directory when a watcher job fails.
 */
@Component
@ConditionalOnProperty(name = "jthumbnailer.watcher.enabled", havingValue = "true")
@Slf4j
public class WatcherFailureHandler implements ThumbnailJobFailureHandler {

    @Override
    public void onFailed(ThumbnailJob job, String errorMessage) {
        if (!job.isWatcherJob() || job.getMoveToFailedDir() == null) return;
        Path source = Path.of(job.getFilePath());
        if (!Files.isRegularFile(source)) return;
        try {
            Path targetDir = Paths.get(job.getMoveToFailedDir());
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(source.getFileName());
            if (target.equals(source)) return;
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(source);
            }
            log.debug("Moved {} to failed {}", source, target);
        } catch (Exception e) {
            log.warn("Could not move file to failed dir: {}", e.getMessage());
        }
    }
}
