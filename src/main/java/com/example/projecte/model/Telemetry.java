package com.example.projecte.model;

import java.time.Instant;

public record Telemetry(
        String deviceId,
        double cpuTemp,      // °C
        double packetRate,   // packets/sec
        Instant timestamp
) {}