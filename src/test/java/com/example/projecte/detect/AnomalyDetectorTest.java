package com.example.projecte.detect;

import com.example.projecte.alert.AlertService;
import com.example.projecte.model.Anomaly;
import com.example.projecte.model.Telemetry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnomalyDetectorTest {

    @Mock
    private AlertService alertService;

    private AnomalyBuffer buffer;
    private SimpleMeterRegistry registry;
    private AnomalyDetector detector;

    @BeforeEach
    void setUp() {
        buffer = new AnomalyBuffer();
        registry = new SimpleMeterRegistry();
        detector = new AnomalyDetector(50, 20, 3.0, buffer, alertService, registry);
    }

    @Test
    void countsProcessedEventsAndSkipsColdStart() {
        feedNormal("edge-0", 20);
        assertEquals(20.0, registry.counter("telemetry_processed_total").count());
        assertEquals(0.0, registry.counter("telemetry_anomalies_total").count());
        assertEquals(0, buffer.size());
        verify(alertService, never()).sendAlert(any(Anomaly.class));
    }

    @Test
    void recordsAnomalyAndSendsAlertAfterWarmup() {
        feedNormal("edge-1", 20);
        detector.inspect(new Telemetry("edge-1", 108.0, 1200.0, Instant.now()));

        assertEquals(1.0, registry.counter("telemetry_anomalies_total").count());
        assertEquals(1, buffer.size());
        assertEquals("cpuTemp", buffer.snapshot().getFirst().metric());
        verify(alertService).sendAlert(any(Anomaly.class));
    }

    private void feedNormal(String deviceId, int n) {
        for (int i = 0; i < n; i++) {
            detector.inspect(new Telemetry(deviceId, 55.0 + (i % 4) * 0.3, 1200.0, Instant.now()));
        }
    }
}
