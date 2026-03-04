package io.github.makbn.jthumbnail.webservice.controller;

import io.github.makbn.jthumbnail.connector.api.JobProducer;
import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;
import io.github.makbn.jthumbnail.webservice.model.JThumbnailApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("/jobs")
@Tag(name = "Jobs API", description = "Persistent thumbnail job status and listing")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JobsController {

    ThumbnailJobService jobService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    JobProducer jobProducer;

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID", description = "Returns the lifecycle state of a thumbnail job.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Job found",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = JobResponse.class))),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<JThumbnailApiResponse<JobResponse>> getJob(
            @Parameter(description = "Job ID", required = true) @PathVariable("id") String id) {
        Optional<ThumbnailJob> job = jobService.findById(id);
        if (job.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(JThumbnailApiResponse.<JobResponse>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .error(true)
                            .message("Job not found")
                            .build());
        }
        return ResponseEntity.ok(JThumbnailApiResponse.<JobResponse>builder()
                .result(toResponse(job.get()))
                .error(false)
                .build());
    }

    @GetMapping
    @Operation(
            summary = "List jobs by status",
            description = "Returns jobs filtered by status. Example: GET /jobs?status=FAILED")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "List of jobs",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<JThumbnailApiResponse<List<JobResponse>>> listJobs(
            @Parameter(description = "Filter by status (PENDING, PROCESSING, FAILED, COMPLETED)")
                    @RequestParam(name = "status", required = false)
                    String status) {
        List<ThumbnailJob> jobs;
        if (status != null && !status.isBlank()) {
            ThumbnailJob.JobStatus s;
            try {
                s = ThumbnailJob.JobStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(JThumbnailApiResponse.<List<JobResponse>>builder()
                                .code(HttpStatus.BAD_REQUEST.value())
                                .error(true)
                                .message("Invalid status: " + status)
                                .build());
            }
            jobs = jobService.findByStatus(s);
        } else {
            jobs = jobService.findAll();
        }
        List<JobResponse> result = jobs.stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(JThumbnailApiResponse.<List<JobResponse>>builder()
                .result(result)
                .error(false)
                .build());
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry a failed job", description = "Re-queues a FAILED job for processing. Requires Kafka.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Job queued for retry"),
        @ApiResponse(responseCode = "400", description = "Job not retryable or Kafka not available"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<JThumbnailApiResponse<JobResponse>> retryJob(
            @Parameter(description = "Job ID", required = true) @PathVariable("id") String id) {
        Optional<ThumbnailJob> jobOpt = jobService.findById(id);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(JThumbnailApiResponse.<JobResponse>builder()
                            .code(HttpStatus.NOT_FOUND.value())
                            .error(true)
                            .message("Job not found")
                            .build());
        }
        ThumbnailJob job = jobOpt.get();
        if (job.getStatus() != ThumbnailJob.JobStatus.FAILED) {
            return ResponseEntity.badRequest()
                    .body(JThumbnailApiResponse.<JobResponse>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .error(true)
                            .message("Only FAILED jobs can be retried")
                            .build());
        }
        if (jobProducer == null) {
            return ResponseEntity.badRequest()
                    .body(JThumbnailApiResponse.<JobResponse>builder()
                            .code(HttpStatus.BAD_REQUEST.value())
                            .error(true)
                            .message("Retry not available (job queue disabled)")
                            .build());
        }
        jobService.markPendingForRetry(job.getJobId(), "Manual retry from admin");
        jobProducer.sendJob(job.getJobId());
        return ResponseEntity.ok(JThumbnailApiResponse.<JobResponse>builder()
                .result(toResponse(jobService.findById(id).orElse(job)))
                .error(false)
                .message("Job queued for retry")
                .build());
    }

    @GetMapping(
            value = "/{id}/thumbnail",
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    @Operation(
            summary = "Preview thumbnail image",
            description = "Returns the generated thumbnail image for a COMPLETED job.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thumbnail image"),
        @ApiResponse(responseCode = "404", description = "Job not found or thumbnail not available")
    })
    public ResponseEntity<Resource> getThumbnailImage(
            @Parameter(description = "Job ID", required = true) @PathVariable("id") String id) throws IOException {
        Optional<ThumbnailJob> jobOpt = jobService.findById(id);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ThumbnailJob job = jobOpt.get();
        if (job.getStatus() != ThumbnailJob.JobStatus.COMPLETED || job.getThumbnailPath() == null) {
            return ResponseEntity.notFound().build();
        }
        java.nio.file.Path path = java.nio.file.Path.of(job.getThumbnailPath());
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new PathResource(path);
        String contentType = Files.probeContentType(path);
        MediaType mediaType = contentType != null && contentType.startsWith("image/")
                ? MediaType.parseMediaType(contentType)
                : MediaType.IMAGE_PNG;
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }

    private JobResponse toResponse(ThumbnailJob job) {
        return JobResponse.builder()
                .jobId(job.getJobId())
                .filePath(job.getFilePath())
                .status(job.getStatus().name())
                .retryCount(job.getRetryCount())
                .errorMessage(job.getErrorMessage())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .thumbnailPath(job.getThumbnailPath())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class JobResponse {
        private String jobId;
        private String filePath;
        private String status;
        private int retryCount;
        private String errorMessage;
        private java.time.Instant createdAt;
        private java.time.Instant completedAt;
        private String thumbnailPath;
    }
}
