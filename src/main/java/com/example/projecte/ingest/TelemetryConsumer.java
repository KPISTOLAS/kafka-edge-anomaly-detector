package com.example.projecte.ingest;

import com.example.projecte.detect.AnomalyDetector;
import com.example.projecte.model.Telemetry;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryConsumer {

    private final AnomalyDetector detector;

    public TelemetryConsumer(AnomalyDetector detector) {
        this.detector = detector;
    }

    @KafkaListener(topics = "${app.topic}")
    public void onMessage(Telemetry telemetry) {
        detector.inspect(telemetry);
    }
}
