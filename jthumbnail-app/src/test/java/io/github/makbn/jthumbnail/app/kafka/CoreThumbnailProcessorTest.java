package io.github.makbn.jthumbnail.app.kafka;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import io.github.makbn.jthumbnail.core.JThumbnailer;
import io.github.makbn.jthumbnail.core.listener.ThumbnailListener;
import io.github.makbn.jthumbnail.core.model.ThumbnailCandidate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

class CoreThumbnailProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void createThumbnailReturnsNullForNullInput() {
        JThumbnailer thumbnailer = mock(JThumbnailer.class);
        CoreThumbnailProcessor processor = new CoreThumbnailProcessor(thumbnailer);
        assertNull(processor.createThumbnail(null));
    }

    @Test
    void createThumbnailReturnsNullForNonExistentFile() {
        JThumbnailer thumbnailer = mock(JThumbnailer.class);
        CoreThumbnailProcessor processor = new CoreThumbnailProcessor(thumbnailer);
        File missing = new File(tempDir.toFile(), "missing.pdf");
        assertNull(processor.createThumbnail(missing));
    }

    @Test
    void createThumbnailReturnsFileWhenListenerReportsReady() throws IOException {
        File input = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(input.toPath(), "content");
        File thumbFile = Files.createTempFile(tempDir, "out", ".png").toFile();

        JThumbnailer thumbnailer = mock(JThumbnailer.class);
        org.mockito.Mockito.doAnswer(inv -> {
                    ThumbnailListener listener = inv.getArgument(1);
                    listener.onThumbnailReady(
                            inv.getArgument(0, ThumbnailCandidate.class).getUid(), thumbFile);
                    return null;
                })
                .when(thumbnailer)
                .run(any(ThumbnailCandidate.class), any(ThumbnailListener.class));

        CoreThumbnailProcessor processor = new CoreThumbnailProcessor(thumbnailer);
        File result = processor.createThumbnail(input);
        assertSame(thumbFile, result);
    }

    @Test
    void createThumbnailReturnsNullWhenListenerReportsFailure() throws IOException {
        File input = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(input.toPath(), "content");

        JThumbnailer thumbnailer = mock(JThumbnailer.class);
        org.mockito.Mockito.doAnswer(inv -> {
                    ThumbnailListener listener = inv.getArgument(1);
                    listener.onThumbnailFailed("uid", "Unsupported format", 500);
                    return null;
                })
                .when(thumbnailer)
                .run(any(ThumbnailCandidate.class), any(ThumbnailListener.class));

        CoreThumbnailProcessor processor = new CoreThumbnailProcessor(thumbnailer);
        File result = processor.createThumbnail(input);
        assertNull(result);
    }

    @Test
    void createThumbnailReturnsNullOnTimeout() throws IOException {
        File input = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(input.toPath(), "content");
        JThumbnailer thumbnailer = mock(JThumbnailer.class);
        // Never invoke listener so latch.await times out
        org.mockito.Mockito.doAnswer(inv -> null)
                .when(thumbnailer)
                .run(any(ThumbnailCandidate.class), any(ThumbnailListener.class));

        CoreThumbnailProcessor processor = new CoreThumbnailProcessor(thumbnailer, 1L);
        File result = processor.createThumbnail(input);
        assertNull(result);
    }

    @Test
    void createThumbnailReturnsNullWhenInterrupted() throws IOException, InterruptedException {
        File input = Files.createTempFile(tempDir, "doc", ".txt").toFile();
        Files.writeString(input.toPath(), "content");
        JThumbnailer thumbnailer = mock(JThumbnailer.class);
        org.mockito.Mockito.doAnswer(inv -> null)
                .when(thumbnailer)
                .run(any(ThumbnailCandidate.class), any(ThumbnailListener.class));

        CoreThumbnailProcessor processor = new CoreThumbnailProcessor(thumbnailer, 300L);
        AtomicReference<File> resultRef = new AtomicReference<>();
        Thread t = new Thread(() -> resultRef.set(processor.createThumbnail(input)));
        t.start();
        Thread.sleep(150);
        t.interrupt();
        t.join(3000);
        assertNull(resultRef.get());
    }
}
