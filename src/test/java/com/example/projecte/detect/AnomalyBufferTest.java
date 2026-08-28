package com.example.projecte.detect;

import com.example.projecte.model.Anomaly;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnomalyBufferTest {

    @Test
    void dropsOldestWhenCapacityExceeded() {
        AnomalyBuffer buffer = new AnomalyBuffer();
        for (int i = 0; i < AnomalyBuffer.MAX_SIZE + 5; i++) {
            buffer.add(new Anomaly("edge-" + i, "cpuTemp", 100.0, 4.0, Instant.now()));
        }
        assertEquals(AnomalyBuffer.MAX_SIZE, buffer.size());
        assertEquals("edge-5", buffer.snapshot().getFirst().deviceId());
    }
}
