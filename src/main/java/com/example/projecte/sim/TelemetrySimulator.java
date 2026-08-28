package com.example.projecte.sim;

import com.example.projecte.model.Telemetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(name = "app.simulator.enabled", havingValue = "true")
public class TelemetrySimulator {

    private final KafkaTemplate<String, Telemetry> kafka;
    private final String topic;
    private final int devices;
    private final int batchPerTick;
    private final double anomalyRate;

    public TelemetrySimulator(
            KafkaTemplate<String, Telemetry> kafka,
            @Value("${app.topic}") String topic,
            @Value("${app.simulator.devices}") int devices,
            @Value("${app.simulator.batch-per-tick}") int batchPerTick,
            @Value("${app.simulator.anomaly-rate}") double anomalyRate) {
        this.kafka = kafka;
        this.topic = topic;
        this.devices = devices;
        this.batchPerTick = batchPerTick;
        this.anomalyRate = anomalyRate;
    }

    @Scheduled(fixedRate = 1000)
    public void emit() {
        var rnd = ThreadLocalRandom.current();
        Instant now = Instant.now();
        for (int i = 0; i < batchPerTick; i++) {
            String deviceId = "edge-" + rnd.nextInt(devices);
            double cpuTemp = rnd.nextGaussian() * 3 + 55;
            double packetRate = rnd.nextGaussian() * 100 + 1200;

            if (rnd.nextDouble() < anomalyRate) {
                if (rnd.nextBoolean()) {
                    cpuTemp = rnd.nextDouble(95, 110);
                } else {
                    packetRate = rnd.nextDouble(9000, 12_000);
                }
            }

            kafka.send(topic, deviceId, new Telemetry(deviceId, cpuTemp, packetRate, now));
        }
    }
}
