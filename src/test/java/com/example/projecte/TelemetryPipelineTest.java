package com.example.projecte;

import com.example.projecte.detect.AnomalyBuffer;
import com.example.projecte.model.Telemetry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.simulator.enabled=false",
        "app.detector.min-samples=5",
        "app.detector.window-size=20",
        "app.detector.z-threshold=3.0",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.listener.auto-startup=true",
        "spring.kafka.admin.auto-create=true"
})
@Import(KafkaTestcontainersConfig.class)
@AutoConfigureMockMvc
class TelemetryPipelineTest {

    private static final String DEVICE = "it-edge-1";

    @Autowired
    private KafkaTemplate<String, Telemetry> kafka;

    @Autowired
    private AnomalyBuffer buffer;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void consumesWarmBaselineThenFlagsCpuSpike() throws Exception {
        Instant now = Instant.now();
        for (int i = 0; i < 8; i++) {
            send(new Telemetry(DEVICE, 55.0 + (i % 3) * 0.2, 1200.0, now));
        }
        send(new Telemetry(DEVICE, 108.0, 1200.0, now));

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(buffer.snapshot())
                    .anyMatch(a -> DEVICE.equals(a.deviceId())
                            && "cpuTemp".equals(a.metric())
                            && a.zScore() >= 3.0);
        });

        mockMvc.perform(get("/api/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bufferedAnomalies").value(1))
                .andExpect(jsonPath("$.byDevice['" + DEVICE + "']").value(1));
    }

    private void send(Telemetry telemetry) throws Exception {
        kafka.send("telemetry", telemetry.deviceId(), telemetry)
                .get(5, TimeUnit.SECONDS);
    }
}
