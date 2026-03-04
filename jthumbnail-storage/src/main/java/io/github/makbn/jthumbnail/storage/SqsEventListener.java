package io.github.makbn.jthumbnail.storage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Optional: poll SQS queue for S3 event notifications and trigger thumbnail jobs.
 * Enable when jthumbnailer.storage.sqs-queue-url is set.
 */
@Component
@ConditionalOnProperty(name = "jthumbnailer.storage.sqs-queue-url")
@Slf4j
public class SqsEventListener {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final StorageProperties props;
    private final SqsClient sqsClient;
    private final S3ThumbnailTriggerService triggerService;

    public SqsEventListener(StorageProperties props, SqsClient sqsClient, S3ThumbnailTriggerService triggerService) {
        this.props = props;
        this.sqsClient = sqsClient;
        this.triggerService = triggerService;
    }

    @PostConstruct
    void logStart() {
        log.info("SQS event listener active for queue: {}", props.sqsQueueUrl());
    }

    @Scheduled(fixedDelayString = "${jthumbnailer.storage.sqs-poll-interval-ms:5000}")
    void poll() {
        String url = props.sqsQueueUrl();
        if (url == null || url.isBlank()) return;
        ReceiveMessageRequest req = ReceiveMessageRequest.builder()
                .queueUrl(url)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build();
        List<Message> messages = sqsClient.receiveMessage(req).messages();
        for (Message msg : messages) {
            try {
                String body = extractS3EventBody(msg.body());
                if (body != null) {
                    triggerService.processEventPayload(body);
                }
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(url)
                        .receiptHandle(msg.receiptHandle())
                        .build());
            } catch (Exception e) {
                log.warn("SQS message processing failed: {}", e.getMessage());
            }
        }
    }

    /** Unwrap SNS envelope if present; otherwise return body as-is. */
    private String extractS3EventBody(String body) throws Exception {
        if (body == null || body.isBlank()) return null;
        JsonNode root = MAPPER.readTree(body);
        if (root.has("Message")) {
            return root.get("Message").asText();
        }
        return body;
    }
}
