package io.github.makbn.jthumbnail.app.kafka;

import io.github.makbn.JThumbnailerStarter;
import io.github.makbn.jthumbnail.api.ThumbnailProcessor;
import io.github.makbn.jthumbnail.core.JThumbnailer;
import io.github.makbn.jthumbnail.core.listener.ThumbnailListener;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ThumbnailProcessor implementation that uses the core JThumbnailer. Bootstraps the application
 * context via {@link JThumbnailerStarter#init(String[])} and runs thumbnail generation
 * synchronously using a listener and latch.
 */
@Slf4j
public class CoreThumbnailProcessor implements ThumbnailProcessor {

    private static final long TIMEOUT_SECONDS = 120L;

    private final JThumbnailer thumbnailer;
    private final long timeoutSeconds;

    /**
     * Create a processor using the shared JThumbnailer instance from {@link
     * JThumbnailerStarter#init(String[])}.
     */
    public CoreThumbnailProcessor() {
        this(JThumbnailerStarter.init(new String[] {}), TIMEOUT_SECONDS);
    }

    /**
     * Create a processor with an existing JThumbnailer (e.g. for testing).
     *
     * @param thumbnailer the thumbnailer to use
     */
    public CoreThumbnailProcessor(JThumbnailer thumbnailer) {
        this(thumbnailer, TIMEOUT_SECONDS);
    }

    /**
     * Create a processor with custom timeout (for testing).
     *
     * @param thumbnailer the thumbnailer to use
     * @param timeoutSeconds timeout for waiting on thumbnail result
     */
    CoreThumbnailProcessor(JThumbnailer thumbnailer, long timeoutSeconds) {
        this.thumbnailer = thumbnailer;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public File createThumbnail(File input) {
        if (input == null || !input.exists()) {
            log.warn("Input file is null or does not exist: {}", input);
            return null;
        }
        String uid = "kafka-" + input.getName() + "-" + System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<File> result = new AtomicReference<>();
        AtomicReference<String> failureMessage = new AtomicReference<>();

        ThumbnailListener listener = new ThumbnailListener() {
            @Override
            public void onThumbnailReady(String hash, File thumbnail) {
                result.set(thumbnail);
                latch.countDown();
            }

            @Override
            public void onThumbnailFailed(String hash, String message, int code) {
                failureMessage.set(message);
                latch.countDown();
            }
        };

        io.github.makbn.jthumbnail.core.model.ThumbnailCandidate candidate =
                io.github.makbn.jthumbnail.core.model.ThumbnailCandidate.of(input, uid);
        thumbnailer.run(candidate, listener);

        try {
            if (!latch.await(timeoutSeconds, TimeUnit.SECONDS)) {
                log.error("Thumbnail generation timed out for: {}", input.getAbsolutePath());
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for thumbnail: {}", input.getAbsolutePath());
            return null;
        }

        if (failureMessage.get() != null) {
            log.warn("Thumbnail generation failed for {}: {}", input.getAbsolutePath(), failureMessage.get());
            return null;
        }
        return result.get();
    }
}
