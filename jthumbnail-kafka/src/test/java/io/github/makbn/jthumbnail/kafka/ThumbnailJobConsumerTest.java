package io.github.makbn.jthumbnail.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.makbn.jthumbnail.connector.api.JobProducer;
import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobProcessor;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ThumbnailJobConsumerTest {

    @Mock
    ThumbnailJobService jobService;

    @Mock
    JobProducer jobProducer;

    @Mock
    ThumbnailJobProcessor processor;

    private ThumbnailJobConsumer consumer;
    private JobQueueProperties props;

    @BeforeEach
    void setUp() {
        props = new JobQueueProperties("test-topic", "test-dlq", 2, 1);
        consumer = new ThumbnailJobConsumer(jobService, jobProducer, props, processor);
    }

    @Test
    void consumeIgnoresEmptyJobId() {
        consumer.consume("");
        consumer.consume("   ");
        consumer.consume(null);
        verify(jobService, never()).findById(any());
    }

    @Test
    void consumeWhenJobNotFoundDoesNotProcess() {
        when(jobService.findById("missing")).thenReturn(Optional.empty());
        consumer.consume("missing");
        verify(jobService).findById("missing");
        verify(jobProducer, never()).sendJob(any());
    }

    @Test
    void consumeWhenJobNotPendingSkipsProcessing() {
        ThumbnailJob job = ThumbnailJob.create("/tmp/file.pdf");
        job.setStatus(ThumbnailJob.JobStatus.PROCESSING);
        when(jobService.findById(job.getJobId())).thenReturn(Optional.of(job));
        when(processor.process(any(ThumbnailJob.class), eq(2))).thenReturn(ThumbnailJobProcessor.ProcessResult.SKIPPED);
        consumer.consume(job.getJobId());
        verify(jobService).findById(job.getJobId());
        verify(processor).process(job, 2);
        verify(jobProducer, never()).sendJob(any());
        verify(jobProducer, never()).sendToDeadLetter(any());
    }
}
