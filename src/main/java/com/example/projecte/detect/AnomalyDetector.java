package com.example.projecte.detect;

import com.example.projecte.alert.AlertService;
import com.example.projecte.model.Anomaly;
import com.example.projecte.model.Telemetry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnomalyDetector {

    private final int windowSize;
    private final int minSamples;
    private final double zThreshold;

    private final AnomalyBuffer buffer;
    private final AlertService alertService;
    private final Counter processed;
    private final Counter anomaliesFound;

    private final Map<String, DeviceWindow> windows = new ConcurrentHashMap<>();

    public AnomalyDetector(
            @Value("${app.detector.window-size:50}") int windowSize,
            @Value("${app.detector.min-samples:20}") int minSamples,
            @Value("${app.detector.z-threshold:3.0}") double zThreshold,
            AnomalyBuffer buffer,
            AlertService alertService,
            MeterRegistry registry) {
        this.windowSize = windowSize;
        this.minSamples = minSamples;
        this.zThreshold = zThreshold;
        this.buffer = buffer;
        this.alertService = alertService;
        this.processed = Counter.builder("telemetry_processed_total")
                .description("Telemetry events consumed and inspected")
                .register(registry);
        this.anomaliesFound = Counter.builder("telemetry_anomalies_total")
                .description("Anomalies flagged by z-score detection")
                .register(registry);
    }

    public void inspect(Telemetry telemetry) {
        processed.increment();
        check(telemetry.deviceId(), "cpuTemp", telemetry.cpuTemp());
        check(telemetry.deviceId(), "packetRate", telemetry.packetRate());
    }

    private void check(String deviceId, String metric, double value) {
        String key = deviceId + "|" + metric;
        DeviceWindow window = windows.computeIfAbsent(
                key, k -> new DeviceWindow(windowSize, minSamples, zThreshold));
        Double z = window.evaluate(value);
        if (z != null) {
            anomaliesFound.increment();
            Anomaly anomaly = new Anomaly(deviceId, metric, value, z, Instant.now());
            buffer.add(anomaly);
            alertService.sendAlert(anomaly);
        }
    }
}
