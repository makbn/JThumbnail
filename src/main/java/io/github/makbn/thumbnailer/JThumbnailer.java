package io.github.makbn.thumbnailer;

import io.github.makbn.thumbnailer.exception.ThumbnailerException;
import io.github.makbn.thumbnailer.exception.ThumbnailerRuntimeException;
import io.github.makbn.thumbnailer.listener.ThumbnailListener;
import io.github.makbn.thumbnailer.model.ThumbnailCandidate;
import io.github.makbn.thumbnailer.util.mime.MimeTypeDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.Closeable;
import java.io.File;

@Configuration
@EnableAsync
public class JThumbnailer implements Closeable {
    private final ThumbnailerManager manager;

    private final MimeTypeDetector typeDetector;

    @Autowired
    public JThumbnailer(ThumbnailerManager manager) {
        this.manager = manager;
        this.typeDetector = new MimeTypeDetector();
    }

    @Async("async_thread_pool_task_executor")
    public void run(ThumbnailCandidate candidate, ThumbnailListener listener) {
        try {
            candidate.setThumbExt(typeDetector.getOutputExt(candidate.getFile()));
            File out = manager.createThumbnail(candidate.getFile(), candidate.getThumbExt());
            listener.onThumbnailReady(candidate.getHash(), out);
        } catch (ThumbnailerRuntimeException | ThumbnailerException re) {
            listener.onThumbnailFailed(candidate.getHash(), re.getMessage(), 500);
        }

    }

    @Override
    public synchronized void close(){
        manager.close();
    }

}
