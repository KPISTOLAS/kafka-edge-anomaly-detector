package com.example.projecte.detect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeviceWindowTest {

    @Test
    void ignoresSamplesUntilMinWindowIsWarm() {
        DeviceWindow window = new DeviceWindow(50, 20, 3.0);
        for (int i = 0; i < 19; i++) {
            assertNull(window.evaluate(55.0 + (i % 3) * 0.1));
        }
        assertEquals(19, window.size());
    }

    @Test
    void doesNotFlagInRangeValuesAfterWarmup() {
        DeviceWindow window = warmBaseline();
        assertNull(window.evaluate(55.2));
    }

    @Test
    void flagsSpikeWithAbsoluteZScoreAtOrAboveThreshold() {
        DeviceWindow window = warmBaseline();
        Double z = window.evaluate(105.0);
        assertNotNull(z);
        assertTrue(Math.abs(z) >= 3.0, "z-score was " + z);
    }

    @Test
    void capsWindowAtMaxSize() {
        DeviceWindow window = new DeviceWindow(10, 5, 3.0);
        for (int i = 0; i < 25; i++) {
            window.evaluate(50.0 + i * 0.01);
        }
        assertEquals(10, window.size());
    }

    private static DeviceWindow warmBaseline() {
        DeviceWindow window = new DeviceWindow(50, 20, 3.0);
        for (int i = 0; i < 20; i++) {
            window.evaluate(55.0 + (i % 5) * 0.2);
        }
        return window;
    }
}
