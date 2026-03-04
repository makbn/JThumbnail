package io.github.makbn.jthumbnail.amqp;

import io.github.makbn.jthumbnail.core.job.ThumbnailJob;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobProcessor;
import io.github.makbn.jthumbnail.core.job.ThumbnailJobService;
import io.github.makbn.jthumbnail.core.metrics.ThumbnailMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * AMQP consumer for thumbnail requests. Supports manual ack, retry (re-queue by jobId), and DLQ.
 * Listens to the configured queue; message body is either a jobId (retry) or JSON AmqpThumbnailMessage (new).
 */
@Component
@ConditionalOnProperty(name = "jthumbnailer.amqp.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class AmqpThumbnailConsumer {

    private final AmqpProperties props;
    private final ThumbnailJobService jobService;
    private final ThumbnailJobProcessor processor;
    private final FileUrlResolver fileUrlResolver;
    private final RabbitTemplate rabbitTemplate;
    private final ThumbnailMetrics metrics;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${jthumbnailer.amqp.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String body = message.getBody() != null ? new String(message.getBody(), StandardCharsets.UTF_8) : "";
        if (body.isBlank()) {
            log.warn("Ignoring empty AMQP message");
            channel.basicAck(deliveryTag, false);
            return;
        }
        // Converter may wrap string in JSON quotes
        String normalized = body.startsWith("\"") && body.endsWith("\"") && body.length() >= 2
                ? body.substring(1, body.length() - 1)
                : body;

        ThumbnailJob job;
        if (FileUrlResolver.looksLikeJobId(normalized)) {
            Optional<ThumbnailJob> found = jobService.findById(body.trim());
            if (found.isEmpty()) {
                log.warn("Job not found for retry: {}", normalized);
                channel.basicAck(deliveryTag, false);
                return;
            }
            job = found.get();
        } else {
            try {
                AmqpThumbnailMessage amqpMessage = objectMapper.readValue(body, AmqpThumbnailMessage.class);
                String localPath = fileUrlResolver.resolveToLocalPath(amqpMessage.getFileUrl());
                job = jobService.createJob(localPath);
                metrics.recordRequest();
            } catch (Exception e) {
                log.error("Invalid AMQP message, sending to DLQ: {}", e.getMessage());
                sendToDlq(body);
                channel.basicAck(deliveryTag, false);
                return;
            }
        }

        ThumbnailJobProcessor.ProcessResult result = processor.process(job, props.maxRetries());

        switch (result) {
            case SUCCESS, SKIPPED -> channel.basicAck(deliveryTag, false);
            case RETRY -> {
                rabbitTemplate.convertAndSend(props.exchange(), props.routingKey(), job.getJobId());
                channel.basicAck(deliveryTag, false);
            }
            case DLQ -> {
                sendToDlq(job.getJobId());
                channel.basicAck(deliveryTag, false);
            }
        }
    }

    private void sendToDlq(Message originalMessage) {
        String body =
                originalMessage.getBody() != null ? new String(originalMessage.getBody(), StandardCharsets.UTF_8) : "";
        sendToDlq(body);
    }

    private void sendToDlq(String body) {
        try {
            rabbitTemplate.convertAndSend(props.deadLetterExchange(), props.deadLetterQueue(), body);
            log.warn("Sent to DLQ: {}", body);
        } catch (Exception e) {
            log.error("Failed to send to DLQ: {}", e.getMessage());
        }
    }
}
