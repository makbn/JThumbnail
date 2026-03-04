package io.github.makbn.jthumbnail.core;

import io.github.makbn.jthumbnail.core.exception.ThumbnailException;
import io.github.makbn.jthumbnail.core.exception.ThumbnailRuntimeException;
import io.github.makbn.jthumbnail.core.listener.ThumbnailListener;
import io.github.makbn.jthumbnail.core.model.ThumbnailCandidate;
import io.github.makbn.jthumbnail.core.model.ThumbnailConfig;
import io.github.makbn.jthumbnail.core.model.ThumbnailEvent;
import io.github.makbn.jthumbnail.core.util.ThumbnailPostProcessor;
import io.github.makbn.jthumbnail.core.util.mime.MimeTypeDetector;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

@Component
@EnableAsync
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class JThumbnailer implements Closeable {
    ThumbnailerManager manager;
    MimeTypeDetector typeDetector;
    ApplicationEventPublisher events;

    public JThumbnailer(ThumbnailerManager manager, ApplicationEventPublisher events, MimeTypeDetector typeDetector) {
        this.manager = manager;
        this.events = events;
        this.typeDetector = typeDetector;
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

            ThumbnailConfig config = candidate.getConfig();
            if (config != null && !config.isNoOp()) {
                ThumbnailPostProcessor.applyConfig(out, config);
            }

            listener.onThumbnailReady(candidate.getUid(), out);
        } catch (ThumbnailRuntimeException | ThumbnailException | IOException re) {
            listener.onThumbnailFailed(candidate.getUid(), re.getMessage(), 500);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        manager.close();
    }
}
