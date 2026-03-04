package io.github.makbn.jthumbnail.kafka;

import io.github.makbn.jthumbnail.connector.api.JobProducer;
import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobProcessor;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(KafkaTemplate.class)
@Slf4j
@RequiredArgsConstructor
public class ThumbnailJobConsumer {

    private final ThumbnailJobService jobService;
    private final JobProducer jobProducer;
    private final JobQueueProperties props;
    private final ThumbnailJobProcessor processor;

    @KafkaListener(
            topics = "${jthumbnailer.jobs.topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "${jthumbnailer.jobs.consumer-concurrency:1}")
    public void consume(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            log.warn("Ignoring empty job id");
            return;
        }
        jobService.findById(jobId).ifPresentOrElse(this::processJob, () -> log.warn("Job not found: {}", jobId));
    }

    private void processJob(ThumbnailJob job) {
        ThumbnailJobProcessor.ProcessResult result = processor.process(job, props.maxRetries());
        switch (result) {
            case SUCCESS, SKIPPED -> {}
            case RETRY -> jobProducer.sendJob(job.getJobId());
            case DLQ -> jobProducer.sendToDeadLetter(job.getJobId());
        }
    }
}
