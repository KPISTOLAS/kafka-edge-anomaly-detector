package com.example.projecte.model;

import java.time.Instant;

public record Anomaly(
        String deviceId,
        String metric,      // "cpuTemp" or "packetRate"
        double value,
        double zScore,
        Instant detectedAt
) {}