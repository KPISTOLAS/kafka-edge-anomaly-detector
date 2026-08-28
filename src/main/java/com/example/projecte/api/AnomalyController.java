package com.example.projecte.api;

import com.example.projecte.detect.AnomalyBuffer;
import com.example.projecte.model.Anomaly;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnomalyController {

    private final AnomalyBuffer buffer;

    public AnomalyController(AnomalyBuffer buffer) {
        this.buffer = buffer;
    }

    @GetMapping("/anomalies")
    public List<Anomaly> anomalies() {
        return buffer.snapshot();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return Map.of(
                "bufferedAnomalies", buffer.size(),
                "byDevice", buffer.countsByDevice());
    }
}
