package io.github.makbn.jthumbnail.graphql;

import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * GraphQL façade over the thumbnail job pipeline.
 *
 * <p>Supports:
 * <ul>
 *   <li>Submitting jobs from a local file path</li>
 *   <li>Querying job status and metadata</li>
 *   <li>Subscribing to job completion events</li>
 * </ul>
 */
@Controller
@RequiredArgsConstructor
@ConditionalOnProperty(name = "jthumbnailer.graphql.enabled", havingValue = "true")
public class ThumbnailGraphQlController {

    private final ThumbnailJobSubmitter jobSubmitter;
    private final ThumbnailJobService jobService;

    @MutationMapping
    public SubmitJobPayload submitThumbnailJob(@Argument String localFilePath) {
        String jobId = jobSubmitter.submit(localFilePath);
        return new SubmitJobPayload(jobId);
    }

    @QueryMapping
    public ThumbnailJobDto thumbnailJob(@Argument String jobId) {
        return jobService.findById(jobId).map(ThumbnailGraphQlController::toDto).orElse(null);
    }

    @QueryMapping
    public List<ThumbnailJobDto> thumbnailJobsByStatus(@Argument ThumbnailJob.JobStatus status) {
        return jobService.findByStatus(status).stream()
                .map(ThumbnailGraphQlController::toDto)
                .toList();
    }

    @SubscriptionMapping
    public Publisher<ThumbnailJobDto> thumbnailJobCompleted(@Argument String jobId) {
        // Simple polling-based subscription: periodically check job status until terminal.
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(ignore -> {
                    Optional<ThumbnailJob> jobOpt = jobService.findById(jobId);
                    if (jobOpt.isEmpty()) {
                        return Flux.empty();
                    }
                    ThumbnailJob job = jobOpt.get();
                    if (job.getStatus() == ThumbnailJob.JobStatus.COMPLETED
                            || job.getStatus() == ThumbnailJob.JobStatus.FAILED) {
                        return Flux.just(toDto(job));
                    }
                    return Flux.empty();
                })
                .take(1);
    }

    private static ThumbnailJobDto toDto(ThumbnailJob job) {
        return new ThumbnailJobDto(
                job.getJobId(),
                job.getFilePath(),
                job.getStatus(),
                job.getRetryCount(),
                job.getErrorMessage(),
                job.getCreatedAt() != null ? job.getCreatedAt().toString() : null,
                job.getCompletedAt() != null ? job.getCompletedAt().toString() : null,
                job.getThumbnailPath(),
                job.getSourceBucket(),
                job.getSourceKey(),
                job.getOutputBucket(),
                job.getOutputKey());
    }

    public record SubmitJobPayload(String jobId) {}

    public record ThumbnailJobDto(
            String jobId,
            String filePath,
            ThumbnailJob.JobStatus status,
            int retryCount,
            String errorMessage,
            String createdAt,
            String completedAt,
            String thumbnailPath,
            String sourceBucket,
            String sourceKey,
            String outputBucket,
            String outputKey) {}
}
