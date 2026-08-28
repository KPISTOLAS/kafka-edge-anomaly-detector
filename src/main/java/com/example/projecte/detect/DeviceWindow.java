package com.example.projecte.detect;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Fixed-size rolling window for one (device, metric) series.
 * {@link #evaluate(double)} is synchronized so parallel Kafka consumers can share the map.
 */
public class DeviceWindow {

    private final Deque<Double> window = new ArrayDeque<>();
    private final int maxSize;
    private final int minSamples;
    private final double zThreshold;

    public DeviceWindow(int maxSize, int minSamples, double zThreshold) {
        this.maxSize = maxSize;
        this.minSamples = minSamples;
        this.zThreshold = zThreshold;
    }

    /**
     * Returns the z-score when {@code |z|} exceeds the threshold and the window is warm,
     * otherwise {@code null}. The sample is always appended after the check (not included
     * in its own z-score) so a spike is judged against the prior baseline.
     */
    public synchronized Double evaluate(double value) {
        Double result = null;
        if (window.size() >= minSamples) {
            double mean = mean();
            double std = std(mean);
            if (std > 1e-9) {
                double z = (value - mean) / std;
                if (Math.abs(z) >= zThreshold) {
                    result = z;
                }
            }
        }
        if (window.size() >= maxSize) {
            window.pollFirst();
        }
        window.addLast(value);
        return result;
    }

    synchronized int size() {
        return window.size();
    }

    private double mean() {
        double sum = 0;
        for (double v : window) {
            sum += v;
        }
        return sum / window.size();
    }

    private double std(double mean) {
        double sq = 0;
        for (double v : window) {
            double d = v - mean;
            sq += d * d;
        }
        return Math.sqrt(sq / window.size());
    }
}
