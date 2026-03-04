package io.github.makbn.jthumbnail.cdnedge;

import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

/**
 * Simple REST connector that accepts a public/signed URL and creates a thumbnail job.
 *
 * <p>Flow:
 * <ol>
 *   <li>Client POSTs a URL (optionally with configuration) to this controller.</li>
 *   <li>Connector downloads the URL to a temp file with safety checks.</li>
 *   <li>Connector submits the local file path via {@link ThumbnailJobSubmitter}.</li>
 *   <li>Core pipeline processes the job and the usual job APIs can be used to query status.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/cdnedge")
@ConditionalOnProperty(name = "jthumbnailer.cdnedge.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CdnEdgeJobController {

    private final CdnEdgeDownloadService downloader;
    private final ThumbnailJobSubmitter jobSubmitter;

    /** Creates a thumbnail job by downloading the given URL and submitting the local file. */
    @PostMapping("/jobs")
    public CreateJobResponse createJob(@Valid @RequestBody CreateJobRequest request) throws IOException {
        File downloaded = downloader.downloadToTemp(request.getUrl());
        String jobId = jobSubmitter.submit(downloaded.getAbsolutePath());
        log.info("CDN edge job created {} for url={}", jobId, request.getUrl());
        return new CreateJobResponse(jobId);
    }

    /** Request body for creating a job from a URL. */
    public static class CreateJobRequest {
        @NotBlank
        private String url;

        /** Default constructor for JSON binding. */
        public CreateJobRequest() {}

        /** Creates a request with the given URL. */
        public CreateJobRequest(String url) {
            this.url = url;
        }

        /** Returns the URL to download. */
        public String getUrl() {
            return url;
        }

        /** Sets the URL to download. */
        public void setUrl(String url) {
            this.url = url;
        }
    }

    /** Response containing the created job ID. */
    @Value
    public static class CreateJobResponse {
        /** The thumbnail job ID. */
        String jobId;
    }
}
