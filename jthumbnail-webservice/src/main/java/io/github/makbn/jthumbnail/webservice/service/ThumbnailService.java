package io.github.makbn.jthumbnail.webservice.service;

import io.github.makbn.jthumbnail.api.model.Thumbnail;
import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import io.github.makbn.jthumbnail.core.JThumbnailer;
import io.github.makbn.jthumbnail.core.config.ThumbnailServerConfiguration;
import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;
import io.github.makbn.jthumbnail.core.metrics.ThumbnailMetrics;
import io.github.makbn.jthumbnail.core.model.ThumbnailCandidate;
import io.github.makbn.jthumbnail.core.model.ThumbnailEvent;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Log4j2
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ThumbnailService {
    JThumbnailer thumbnailer;
    ThumbnailServerConfiguration thumbnailServerConfiguration;
    ThumbnailJobService jobService;
    ThumbnailJobSubmitter jobSubmitter;
    ThumbnailMetrics metrics;
    Map<String, CompletableFuture<Thumbnail>> waitingMap;
    Map<String, File> temporaryFilesMap;

    public ThumbnailService(
            JThumbnailer thumbnailer,
            ThumbnailServerConfiguration settings,
            ThumbnailJobService jobService,
            @Autowired(required = false) ThumbnailJobSubmitter jobSubmitter,
            ThumbnailMetrics metrics) {
        this.thumbnailer = thumbnailer;
        this.thumbnailServerConfiguration = settings;
        this.jobService = jobService;
        this.jobSubmitter = jobSubmitter;
        this.metrics = metrics;
        this.waitingMap = createBoundedMap();
        this.temporaryFilesMap = createBoundedMap();
    }

    /** Creates a persistent job and sends to queue when submitter available; otherwise in-memory. Returns jobId/uid. */
    public String requestThumbnail(@NonNull MultipartFile multipartFile) throws IOException {
        File file = createTempFile(multipartFile);
        if (jobSubmitter != null) {
            return jobSubmitter.submit(file.getAbsolutePath());
        }
        metrics.recordRequest();
        CompletableFuture<Thumbnail> completableFuture = new CompletableFuture<>();
        String uid = UUID.randomUUID().toString();
        ThumbnailCandidate candidate = ThumbnailCandidate.of(file, uid);
        waitingMap.put(uid, completableFuture);
        temporaryFilesMap.put(uid, file);
        thumbnailer.run(candidate);
        return uid;
    }

    public Optional<Thumbnail> getThumbnail(@NonNull String uid) {
        Optional<ThumbnailJob> job = jobService.findById(uid);
        if (job.isPresent()) {
            return jobToThumbnail(job.get());
        }
        if (waitingMap.containsKey(uid)) {
            if (waitingMap.get(uid).isDone()) {
                try {
                    return Optional.ofNullable(waitingMap.get(uid).get());
                } catch (InterruptedException | ExecutionException e) {
                    log.debug(e);
                    Thread.currentThread().interrupt();
                }
            }
            return Optional.of(Thumbnail.builder()
                    .uid(uid)
                    .status(Thumbnail.Status.WAITING)
                    .build());
        }
        return Optional.empty();
    }

    private Optional<Thumbnail> jobToThumbnail(ThumbnailJob job) {
        return switch (job.getStatus()) {
            case PENDING, PROCESSING -> Optional.of(Thumbnail.builder()
                    .uid(job.getJobId())
                    .status(Thumbnail.Status.WAITING)
                    .build());
            case COMPLETED -> Optional.of(Thumbnail.builder()
                    .uid(job.getJobId())
                    .status(Thumbnail.Status.GENERATED)
                    .thumbnailFile(job.getThumbnailPath() != null ? new File(job.getThumbnailPath()) : null)
                    .build());
            case FAILED -> Optional.empty();
        };
    }

    /**
     * Removes a thumbnail processing task associated with the provided UID from the waitingMap.
     * Optionally, it can also remove the temporary uploaded file associated with the UID.
     *
     * @param uid        The unique identifier (UID) associated with the thumbnail processing task.
     * @param removeFile A boolean flag indicating whether to remove the temporary uploaded file.
     */
    public void removeThumbnail(@NonNull String uid, boolean removeFile) {
        if (waitingMap.containsKey(uid)) {
            waitingMap.get(uid).cancel(true);
            waitingMap.remove(uid);
        }
        if (removeFile) {
            removeTemporaryUploadedFile(uid);
        }
    }

    /**
     * Handles a ThumbnailEvent (used only for non-job in-memory path if any).
     *
     * @param event The ThumbnailEvent to process.
     */
    @EventListener
    void onThumbnailEvent(ThumbnailEvent event) {
        if (waitingMap.containsKey(event.getUid())) {
            waitingMap
                    .get(event.getUid())
                    .complete(Thumbnail.builder()
                            .status(Thumbnail.Status.valueOf(event.getStatus().name()))
                            .uid(event.getUid())
                            .thumbnailFile(event.getThumbnailFile())
                            .build());
        }
        removeTemporaryUploadedFile(event.getUid());
    }

    private void removeTemporaryUploadedFile(@NonNull String uid) {
        if (temporaryFilesMap.containsKey(uid)) {
            temporaryFilesMap.get(uid).deleteOnExit();
            temporaryFilesMap.remove(uid);
        }
    }

    /**
     * Creates a temporary File from the provided MultipartFile, using a randomly
     * generated UUID as part of the filename. The file is created in the upload
     * directory specified in the application settings.
     *
     * @param multipartFile The MultipartFile to create a temporary File from.
     * @return A temporary File created from the MultipartFile.
     * @throws IOException If an I/O error occurs during file creation.
     */
    private File createTempFile(@NonNull MultipartFile multipartFile) throws IOException {
        File tempFile = Files.createTempFile(
                        thumbnailServerConfiguration.getUploadDirectory().toPath(),
                        UUID.randomUUID().toString(),
                        multipartFile.getOriginalFilename())
                .toFile();
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    /**
     * Creates a thread-safe {@link Map} with a bounded size, implemented using a
     * {@link LinkedHashMap} wrapped in {@link Collections#synchronizedMap(Map)}.
     * <p>
     * The maximum number of entries is determined by
     * {@code thumbnailServerConfiguration.getMaxWaitingListSize()}. When this limit
     * is exceeded, the eldest entry (based on insertion order) is automatically removed.
     * Each removal is logged at debug level.
     * </p>
     *
     * <p><b>Thread Safety:</b> Synchronization is provided via
     * {@link Collections#synchronizedMap(Map)}, which ensures safe concurrent access
     * at the cost of coarse-grained locking. For higher concurrency or advanced cache
     * policies, consider a dedicated caching library such as Caffeine.</p>
     *
     * @param <T> the type of values stored in the map
     * @return a thread-safe bounded {@link Map} with automatic eviction
     * @see Collections#synchronizedMap(Map)
     */
    private <T> Map<String, T> createBoundedMap() {
        Map<String, T> limitedMap = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
                boolean remove = size() > thumbnailServerConfiguration.getMaxWaitingListSize();
                if (remove) {
                    log.debug("Removing eldest entry: {}", eldest.getKey());
                }
                return remove;
            }
        };
        return Collections.synchronizedMap(limitedMap);
    }
}
