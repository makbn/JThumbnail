package io.github.makbn.jthumbnail.core.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.stereotype.Component;

/**
 * Micrometer metrics for thumbnails: request count, processing duration, success/failure,
 * and per file-type counters. Exposed on {@code /actuator/prometheus}.
 */
@Component
public class ThumbnailMetrics {

    private static final String TAG_FILE_TYPE = "file_type";
    private static final String TAG_STATUS = "status";

    private final MeterRegistry registry;
    private final Counter requestCounter;
    private final Counter processedCounter;
    private final Counter failureCounter;
    private final Timer processingDuration;

    public ThumbnailMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.requestCounter = registry.counter("thumbnail_requests_total");
        this.processedCounter = registry.counter("thumbnail_processed_total", TAG_STATUS, "success");
        this.failureCounter = registry.counter("thumbnail_processed_total", TAG_STATUS, "failure");
        this.processingDuration = registry.timer("thumbnail_processing_duration_seconds");
    }

    /** Record one thumbnail request (e.g. upload or job enqueue). */
    public void recordRequest() {
        requestCounter.increment();
    }

    /** Record successful processing; stops the sample for duration and records file type. */
    public void recordSuccess(String fileType, Timer.Sample sample) {
        if (sample != null) {
            sample.stop(processingDuration);
        }
        processedCounter.increment();
        recordFileType(fileType);
    }

    /** Record failed processing; stops the sample if present and records file type. */
    public void recordFailure(String fileType, Timer.Sample sample) {
        if (sample != null) {
            sample.stop(processingDuration);
        }
        failureCounter.increment();
        recordFileType(fileType);
    }

    private void recordFileType(String fileType) {
        String tag = fileType != null && !fileType.isBlank()
                ? fileType.replace("/", "_").replace(" ", "_")
                : "unknown";
        registry.counter("thumbnail_by_file_type_total", TAG_FILE_TYPE, tag).increment();
    }

    /** Start a sample; pass to {@link #recordSuccess} or {@link #recordFailure} when done. */
    public Timer.Sample startProcessingTimer() {
        return Timer.start();
    }
}
