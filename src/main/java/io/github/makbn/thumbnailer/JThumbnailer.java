package io.github.makbn.thumbnailer;

import io.github.makbn.thumbnailer.exception.FileDoesNotExistException;
import io.github.makbn.thumbnailer.exception.ThumbnailerException;
import io.github.makbn.thumbnailer.exception.ThumbnailerRuntimeException;
import io.github.makbn.thumbnailer.listener.JTConsumerCallback;
import io.github.makbn.thumbnailer.listener.ThumbnailListener;
import io.github.makbn.thumbnailer.model.ThumbnailCandidate;
import io.github.makbn.thumbnailer.thumbnailers.*;
import io.github.makbn.thumbnailer.util.mime.MimeTypeDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

public class JThumbnailer {
    private static final Logger mLog = LogManager.getLogger(JThumbnailer.class.getSimpleName());
    private final static AtomicBoolean started = new AtomicBoolean(false);
    private static ThumbnailerManager thumbnailer;
    private static String defaultOutputDir;
    private static ConcurrentHashMap<ThumbnailCandidate, ThumbnailListener> files;
    private static JThumbnailerShutDownTask shutDownTask;
    private static JTConsumer consumer;

    static {
        start();
    }

    public static int getTaskQueueSize() {
        if (files != null) {
            return files.size();
        }
        return 0;
    }

    public static void createThumbnail(ThumbnailCandidate candidate, ThumbnailListener listener) {
        synchronized (files) {
            files.put(candidate, listener);
            mLog.info("file added to queue and queue notified!");
            files.notifyAll();
        }
    }

    private static void restart() {
        mLog.info("trying to restart JThumbnailer");
        // remove the current shutdown task
        if (shutDownTask != null) {
            Runtime.getRuntime().removeShutdownHook(shutDownTask);
        }
        thumbnailer.close();
        consumer.close();
        started.set(false);
        start();
    }

    private static void start() throws ThumbnailerRuntimeException {
        synchronized (started) {
            if (started.get()) {
                mLog.info("JThumbnailer already started!");
            } else {
                defaultOutputDir = AppSettings.TEMP_DIR;

                files = new ConcurrentHashMap<>();

                thumbnailer = new ThumbnailerManager();
                loadExistingThumbnailers();
                setParameters();
                consumer = new JTConsumer(files, thumbnailer, (e) -> {
                    mLog.error(e);
                    if (AppSettings.HANDLE_RESTART)
                        restart();
                });
                shutDownTask = new JThumbnailerShutDownTask(thumbnailer);
                Runtime.getRuntime().addShutdownHook(shutDownTask);
                started.set(true);
            }
        }
    }

    private static void setParameters() {
        try {
            thumbnailer.setThumbnailFolder(defaultOutputDir);
        } catch (FileDoesNotExistException e) {
            throw new ThumbnailerRuntimeException(e.getMessage());
        }
        thumbnailer.setImageSize(AppSettings.THUMB_WIDTH, AppSettings.THUMB_HEIGHT, 0);
    }

    protected static void loadExistingThumbnailers() {
        thumbnailer.registerThumbnailer(new NativeImageThumbnailer());
        thumbnailer.registerThumbnailer(new OpenOfficeThumbnailer());
        thumbnailer.registerThumbnailer(new PDFBoxThumbnailer());
        thumbnailer.registerThumbnailer(new WordConverterThumbnailer());
        thumbnailer.registerThumbnailer(new ExcelConverterThumbnailer());
        thumbnailer.registerThumbnailer(new PowerpointConverterThumbnailer());
        thumbnailer.registerThumbnailer(new JODHtmlConverterThumbnailer());
        thumbnailer.registerThumbnailer(new MPEGThumbnailer());
        thumbnailer.registerThumbnailer(new MP3Thumbnailer());
        thumbnailer.registerThumbnailer(new DWGThumbnailer());
        thumbnailer.registerThumbnailer(new ImageThumbnailer());
        thumbnailer.registerThumbnailer(new TextThumbnailer());

        mLog.info("Thumbnailers loaded!");
    }

}


class JTConsumer implements Runnable {

    private static final Logger mLog = LogManager.getLogger(JTConsumer.class.getSimpleName());
    private final ThreadPoolExecutor executor;
    private final ConcurrentHashMap<ThumbnailCandidate, ThumbnailListener> files;
    private final MimeTypeDetector typeDetector;
    private final Thread taskConsumer;
    private final AtomicBoolean interrupted = new AtomicBoolean(false);
    private final ThumbnailerManager manager;

    private final JTConsumerCallback callback;

    public JTConsumer(ConcurrentHashMap<ThumbnailCandidate, ThumbnailListener> files, ThumbnailerManager manager, JTConsumerCallback callback) {
        this.files = files;
        this.manager = manager;
        this.callback = callback;
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        this.taskConsumer = new Thread(this);
        this.typeDetector = new MimeTypeDetector();
        taskConsumer.start();
    }


    private File submitFile(File inputFile, String ext) throws ThumbnailerRuntimeException, ThumbnailerException {
        return manager.createThumbnail(inputFile, ext);
    }


    public void close() {
        synchronized (interrupted) {
            interrupted.set(true);
            executor.shutdownNow();
            taskConsumer.interrupt();
            mLog.info("JTConsumer closed successfully!");
        }
    }

    @Override
    public void run() {
        synchronized (files) {
            while (!interrupted.get()) {
                mLog.debug(String.format("try to read from queue:%d", files.size()));
                while (!files.isEmpty() && !interrupted.get()) {
                    files.entrySet().removeIf(e -> {
                        try {
                            Runnable thumbnailTask = () -> {
                                mLog.debug("read from files!");
                                try {
                                    e.getKey().setThumbExt(typeDetector.getOutputExt(e.getKey().getFile()));
                                    File out = submitFile(e.getKey().getFile(), e.getKey().getThumbExt());
                                    e.getValue().onThumbnailReady(e.getKey().getHash(), out);
                                } catch (ThumbnailerRuntimeException | ThumbnailerException re) {
                                    callback.onException(new ThumbnailerRuntimeException(re));
                                    e.getValue().onThumbnailFailed(e.getKey().getHash(), re.getMessage(), 500);
                                }
                            };
                            executor.execute(thumbnailTask);
                        } catch (Exception | IllegalAccessError ex) {
                            callback.onException(new ThumbnailerRuntimeException(ex));
                            e.getValue().onThumbnailFailed(e.getKey().getHash(), ex.getMessage(), 500);
                        }
                        return true;
                    });
                }
                try {
                    files.wait(AppSettings.FORCE_RECHECK_QUEUE_MS);
                } catch (InterruptedException e) {
                    callback.onException(new ThumbnailerRuntimeException(e));
                }
            }
        }
    }
}

class JThumbnailerShutDownTask extends Thread {
    private final ThumbnailerManager manager;

    public JThumbnailerShutDownTask(ThumbnailerManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        if (this.manager != null) {
            manager.close();
        }
    }

}