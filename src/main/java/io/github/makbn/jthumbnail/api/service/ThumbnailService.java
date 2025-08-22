package io.github.makbn.jthumbnail.api.service;

import io.github.makbn.jthumbnail.api.model.Thumbnail;
import io.github.makbn.jthumbnail.core.JThumbnailer;
import io.github.makbn.jthumbnail.core.config.AppSettings;
import io.github.makbn.jthumbnail.core.model.ThumbnailCandidate;
import io.github.makbn.jthumbnail.core.model.ThumbnailEvent;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Log4j2
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ThumbnailService {
    JThumbnailer thumbnailer;
    AppSettings settings;
    // can be replaced with LoadingCache to prevent OOM
    Map<String, CompletableFuture<Thumbnail>> waitingMap;
    // can be replaced with LoadingCache to prevent OOM
    Map<String, File> temporaryFilesMap;

    public ThumbnailService(JThumbnailer thumbnailer, AppSettings settings) {
        this.thumbnailer = thumbnailer;
        this.settings = settings;
        this.waitingMap = new HashMap<>();
        this.temporaryFilesMap = new HashMap<>();
    }

    public String requestThumbnail(@NonNull MultipartFile multipartFile) throws IOException {
        File file = createTempFile(multipartFile);
        CompletableFuture<Thumbnail> completableFuture = new CompletableFuture<>();
        String uid = UUID.randomUUID().toString();
        ThumbnailCandidate candidate = ThumbnailCandidate.of(file, uid);
        waitingMap.put(uid, completableFuture);
        temporaryFilesMap.put(uid, file);
        // after keeping the references, let's create the thumbnail
        thumbnailer.run(candidate);

        return uid;
    }

    public Optional<Thumbnail> getThumbnail(@NonNull String uid) {
        if (waitingMap.containsKey(uid)) {
            if (waitingMap.get(uid).isDone()) {
                try {
                    return Optional.ofNullable(waitingMap.get(uid).get());
                } catch (InterruptedException | ExecutionException e) {
                    log.debug(e);
                    Thread.currentThread().interrupt();
                }

            } else {
                return Optional.of(Thumbnail.builder()
                        .uid(uid)
                        .status(Thumbnail.Status.WAITING)
                        .build());
            }
        }
        return Optional.empty();
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
     * Handles a ThumbnailEvent, processing the event data and completing
     * associated CompletableFuture objects if they exist. This method converts
     * the event information into a Thumbnail object and completes the corresponding
     * CompletableFuture with the thumbnail data.
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

    /**
     * Removes a temporary uploaded file from the temporaryFilesMap and schedules it
     * for deletion upon application exit. The removal process ensures that the file
     * associated with the provided UID is properly cleaned up.
     *
     * @param uid The unique identifier (UID) associated with the temporary uploaded file.
     */
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
                        settings.getUploadDirectory().toPath(),
                        UUID.randomUUID().toString(),
                        multipartFile.getOriginalFilename())
                .toFile();
        multipartFile.transferTo(tempFile);
        return tempFile;
    }
}
