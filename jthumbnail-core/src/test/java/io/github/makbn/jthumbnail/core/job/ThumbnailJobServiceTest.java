package io.github.makbn.jthumbnail.core.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@Import(ThumbnailJobService.class)
@ActiveProfiles("test")
class ThumbnailJobServiceTest {

    @Autowired
    ThumbnailJobService service;

    @Autowired
    ThumbnailJobRepository repository;

    @Test
    void createJobPersistsAndReturnsJob() {
        ThumbnailJob job = service.createJob("/tmp/file.pdf");
        assertTrue(job.getJobId() != null && !job.getJobId().isEmpty());
        assertEquals("/tmp/file.pdf", job.getFilePath());
        assertEquals(ThumbnailJob.JobStatus.PENDING, job.getStatus());
        assertEquals(0, job.getRetryCount());
        assertEquals(1, repository.count());
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        Optional<ThumbnailJob> opt = service.findById("non-existent");
        assertTrue(opt.isEmpty());
    }

    @Test
    void findByIdReturnsJobWhenFound() {
        ThumbnailJob created = service.createJob("/path/to/doc.docx");
        Optional<ThumbnailJob> opt = service.findById(created.getJobId());
        assertTrue(opt.isPresent());
        assertEquals(created.getJobId(), opt.get().getJobId());
    }

    @Test
    void markProcessingUpdatesStatus() {
        ThumbnailJob job = service.createJob("/tmp/a.png");
        Optional<ThumbnailJob> updated = service.markProcessing(job.getJobId());
        assertTrue(updated.isPresent());
        assertEquals(ThumbnailJob.JobStatus.PROCESSING, updated.get().getStatus());
    }

    @Test
    void markCompletedSetsThumbnailPathAndClearsError() {
        ThumbnailJob job = service.createJob("/tmp/b.jpg");
        service.markProcessing(job.getJobId());
        Optional<ThumbnailJob> completed = service.markCompleted(job.getJobId(), "/out/thumb.png");
        assertTrue(completed.isPresent());
        assertEquals(ThumbnailJob.JobStatus.COMPLETED, completed.get().getStatus());
        assertEquals("/out/thumb.png", completed.get().getThumbnailPath());
        assertEquals(null, completed.get().getErrorMessage());
    }

    @Test
    void markFailedIncrementsRetryAndSetsError() {
        ThumbnailJob job = service.createJob("/tmp/c.mp4");
        Optional<ThumbnailJob> failed = service.markFailed(job.getJobId(), "Conversion failed");
        assertTrue(failed.isPresent());
        assertEquals(ThumbnailJob.JobStatus.FAILED, failed.get().getStatus());
        assertEquals(1, failed.get().getRetryCount());
        assertEquals("Conversion failed", failed.get().getErrorMessage());
    }

    @Test
    void markPendingForRetryResetsStatusForRetry() {
        ThumbnailJob job = service.createJob("/tmp/d.pdf");
        service.markFailed(job.getJobId(), "First failure");
        Optional<ThumbnailJob> retry = service.markPendingForRetry(job.getJobId(), "Manual retry");
        assertTrue(retry.isPresent());
        assertEquals(ThumbnailJob.JobStatus.PENDING, retry.get().getStatus());
        assertEquals(2, retry.get().getRetryCount());
    }

    @Test
    void findByStatusReturnsOnlyMatching() {
        ThumbnailJob a = service.createJob("/a");
        ThumbnailJob b = service.createJob("/b");
        service.markFailed(a.getJobId(), "err");
        List<ThumbnailJob> failed = service.findByStatus(ThumbnailJob.JobStatus.FAILED);
        assertEquals(1, failed.size());
        assertEquals(a.getJobId(), failed.get(0).getJobId());
    }

    @Test
    void findAllReturnsAllOrderedByCreatedAtDesc() {
        service.createJob("/first");
        service.createJob("/second");
        List<ThumbnailJob> all = service.findAll();
        assertEquals(2, all.size());
        assertTrue(all.get(0).getCreatedAt().compareTo(all.get(1).getCreatedAt()) >= 0);
    }
}
