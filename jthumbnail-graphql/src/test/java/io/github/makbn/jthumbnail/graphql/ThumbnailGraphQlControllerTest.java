package io.github.makbn.jthumbnail.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.github.makbn.jthumbnail.connector.api.ThumbnailJobSubmitter;
import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

class ThumbnailGraphQlControllerTest {

    @Test
    void submitThumbnailJobReturnsJobId() {
        ThumbnailJobSubmitter submitter = Mockito.mock(ThumbnailJobSubmitter.class);
        ThumbnailJobService jobService = Mockito.mock(ThumbnailJobService.class);
        when(submitter.submit(any(String.class))).thenReturn("job-1");

        ThumbnailGraphQlController controller = new ThumbnailGraphQlController(submitter, jobService);

        ThumbnailGraphQlController.SubmitJobPayload payload = controller.submitThumbnailJob("/tmp/file.pdf");
        org.junit.jupiter.api.Assertions.assertEquals("job-1", payload.jobId());
    }

    @Test
    void thumbnailJobQueryReturnsJob() {
        ThumbnailJob job = ThumbnailJob.create("/tmp/a.txt");
        ThumbnailJobService jobService = Mockito.mock(ThumbnailJobService.class);
        when(jobService.findById(eq(job.getJobId()))).thenReturn(Optional.of(job));
        ThumbnailGraphQlController controller =
                new ThumbnailGraphQlController(Mockito.mock(ThumbnailJobSubmitter.class), jobService);

        ThumbnailGraphQlController.ThumbnailJobDto dto = controller.thumbnailJob(job.getJobId());

        org.junit.jupiter.api.Assertions.assertEquals(job.getJobId(), dto.jobId());
        org.junit.jupiter.api.Assertions.assertEquals("/tmp/a.txt", dto.filePath());
    }

    @Test
    void thumbnailJobsByStatusReturnsList() {
        ThumbnailJob job = ThumbnailJob.create("/tmp/b.txt");
        job.setStatus(ThumbnailJob.JobStatus.PROCESSING);
        job.setRetryCount(1);
        job.setErrorMessage("none");
        job.setThumbnailPath("/tmp/thumb.png");

        ThumbnailJobService jobService = Mockito.mock(ThumbnailJobService.class);
        when(jobService.findByStatus(eq(ThumbnailJob.JobStatus.PROCESSING))).thenReturn(List.of(job));

        ThumbnailGraphQlController controller =
                new ThumbnailGraphQlController(Mockito.mock(ThumbnailJobSubmitter.class), jobService);

        List<ThumbnailGraphQlController.ThumbnailJobDto> list =
                controller.thumbnailJobsByStatus(ThumbnailJob.JobStatus.PROCESSING);

        org.junit.jupiter.api.Assertions.assertEquals(1, list.size());
        org.junit.jupiter.api.Assertions.assertEquals("/tmp/b.txt", list.get(0).filePath());
    }

    @Test
    void thumbnailJobCompletedSubscriptionEmitsWhenCompleted() {
        ThumbnailJob job = ThumbnailJob.create("/tmp/c.txt");
        job.setStatus(ThumbnailJob.JobStatus.COMPLETED);
        job.setThumbnailPath("/tmp/c-thumb.png");

        ThumbnailJobService jobService = Mockito.mock(ThumbnailJobService.class);
        when(jobService.findById(eq(job.getJobId()))).thenReturn(Optional.of(job));

        ThumbnailGraphQlController controller =
                new ThumbnailGraphQlController(Mockito.mock(ThumbnailJobSubmitter.class), jobService);

        reactor.core.publisher.Flux<ThumbnailGraphQlController.ThumbnailJobDto> flux =
                (reactor.core.publisher.Flux<ThumbnailGraphQlController.ThumbnailJobDto>)
                        controller.thumbnailJobCompleted(job.getJobId());

        // Subscription polls every 1s; allow time for first tick to emit
        ThumbnailGraphQlController.ThumbnailJobDto dto = flux.blockFirst(Duration.ofSeconds(3));
        org.junit.jupiter.api.Assertions.assertEquals(job.getJobId(), dto.jobId());
    }
}
