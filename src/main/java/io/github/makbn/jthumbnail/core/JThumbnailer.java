package io.github.makbn.jthumbnail.core;

import io.github.makbn.jthumbnail.core.exception.ThumbnailerException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailerRuntimeException;
import io.github.makbn.jthumbnail.core.listener.ThumbnailListener;
import io.github.makbn.jthumbnail.core.model.ThumbnailCandidate;
import io.github.makbn.jthumbnail.core.model.ThumbnailEvent;
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeDetector;
import java.io.Closeable;
import java.io.File;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@EnableAsync
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class JThumbnailer implements Closeable {
    ThumbnailerManager manager;
    MimeTypeDetector typeDetector;
    ApplicationEventPublisher events;

    public JThumbnailer(ThumbnailerManager manager, ApplicationEventPublisher events) {
        this.manager = manager;
        this.events = events;
        this.typeDetector = new MimeTypeDetector();
    }

    @Async("asyncThreadPoolTaskExecutor")
    public void run(ThumbnailCandidate candidate, ThumbnailListener listener) {
        internalRun(candidate, listener);
    }

    @Async("asyncThreadPoolTaskExecutor")
    public void run(ThumbnailCandidate candidate) {
        this.internalRun(candidate, new ThumbnailListener() {
            @Override
            public void onThumbnailReady(String hash, File thumbnail) {
                events.publishEvent(ThumbnailEvent.builder()
                        .uid(hash)
                        .thumbnailFile(thumbnail)
                        .status(ThumbnailEvent.Status.GENERATED)
                        .build());
            }

            @Override
            public void onThumbnailFailed(String hash, String message, int code) {
                events.publishEvent(ThumbnailEvent.builder()
                        .uid(hash)
                        .thumbnailFile(null)
                        .status(ThumbnailEvent.Status.FAILED)
                        .build());
            }
        });
    }

    private void internalRun(ThumbnailCandidate candidate, ThumbnailListener listener) {
        try {
            candidate.setThumbExt(typeDetector.getOutputExt(candidate.getFile()));
            File out = manager.createThumbnail(candidate.getFile(), candidate.getThumbExt());
            listener.onThumbnailReady(candidate.getUid(), out);
        } catch (ThumbnailerRuntimeException | ThumbnailerException re) {
            listener.onThumbnailFailed(candidate.getUid(), re.getMessage(), 500);
        }
    }

    @Override
    public synchronized void close() {
        manager.close();
    }
}
