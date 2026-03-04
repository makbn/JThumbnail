package io.github.makbn.jthumbnail.core.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThumbnailMetricsTest {

    private MeterRegistry meterRegistry;
    private ThumbnailMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new ThumbnailMetrics(meterRegistry);
    }

    @Test
    void recordRequestIncrementsCounter() {
        metrics.recordRequest();
        metrics.recordRequest();
        assertEquals(2, meterRegistry.get("thumbnail_requests_total").counter().count());
    }

    @Test
    void recordSuccessIncrementsProcessedAndFileType() {
        io.micrometer.core.instrument.Timer.Sample sample = metrics.startProcessingTimer();
        metrics.recordSuccess("image/png", sample);
        assertEquals(
                1,
                meterRegistry
                        .get("thumbnail_processed_total")
                        .tag("status", "success")
                        .counter()
                        .count());
        assertEquals(
                1,
                meterRegistry
                        .get("thumbnail_by_file_type_total")
                        .tag("file_type", "image_png")
                        .counter()
                        .count());
    }

    @Test
    void recordSuccessWithNullSampleStillIncrements() {
        metrics.recordSuccess("video/mp4", null);
        assertEquals(
                1,
                meterRegistry
                        .get("thumbnail_processed_total")
                        .tag("status", "success")
                        .counter()
                        .count());
    }

    @Test
    void recordFailureIncrementsFailureAndFileType() {
        io.micrometer.core.instrument.Timer.Sample sample = metrics.startProcessingTimer();
        metrics.recordFailure("application/pdf", sample);
        assertEquals(
                1,
                meterRegistry
                        .get("thumbnail_processed_total")
                        .tag("status", "failure")
                        .counter()
                        .count());
        assertEquals(
                1,
                meterRegistry
                        .get("thumbnail_by_file_type_total")
                        .tag("file_type", "application_pdf")
                        .counter()
                        .count());
    }

    @Test
    void recordFailureWithNullSample() {
        metrics.recordFailure("unknown", null);
        assertEquals(
                1,
                meterRegistry
                        .get("thumbnail_processed_total")
                        .tag("status", "failure")
                        .counter()
                        .count());
        assertEquals(
                1,
                meterRegistry
                        .get("thumbnail_by_file_type_total")
                        .tag("file_type", "unknown")
                        .counter()
                        .count());
    }

    @Test
    void startProcessingTimerReturnsSample() {
        io.micrometer.core.instrument.Timer.Sample sample = metrics.startProcessingTimer();
        assertNotNull(sample);
    }
}
