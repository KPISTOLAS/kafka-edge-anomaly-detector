package com.example.projecte.detect;

import com.example.projecte.model.Anomaly;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thread-safe ring of the most recent anomalies for the REST API and rolling report.
 */
@Component
public class AnomalyBuffer {

    static final int MAX_SIZE = 200;

    private final Deque<Anomaly> recent = new ArrayDeque<>(MAX_SIZE);

    public synchronized void add(Anomaly anomaly) {
        if (recent.size() >= MAX_SIZE) {
            recent.pollFirst();
        }
        recent.addLast(anomaly);
    }

    public synchronized List<Anomaly> snapshot() {
        return List.copyOf(recent);
    }

    public synchronized int size() {
        return recent.size();
    }

    public Map<String, Long> countsByDevice() {
        return snapshot().stream()
                .collect(Collectors.groupingBy(
                        Anomaly::deviceId,
                        LinkedHashMap::new,
                        Collectors.counting()));
    }
}
