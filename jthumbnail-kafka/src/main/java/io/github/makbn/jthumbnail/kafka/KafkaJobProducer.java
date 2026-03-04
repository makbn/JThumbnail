package io.github.makbn.jthumbnail.kafka;

import io.github.makbn.jthumbnail.connector.api.JobProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(KafkaTemplate.class)
@Slf4j
@RequiredArgsConstructor
public class KafkaJobProducer implements JobProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JobQueueProperties props;

    @Override
    public void sendJob(String jobId) {
        kafkaTemplate.send(props.topic(), jobId, jobId);
        log.debug("Sent job {} to topic {}", jobId, props.topic());
    }

    @Override
    public void sendToDeadLetter(String jobId) {
        kafkaTemplate.send(props.deadLetterTopic(), jobId, jobId);
        log.warn("Sent job {} to dead-letter topic {}", jobId, props.deadLetterTopic());
    }
}
