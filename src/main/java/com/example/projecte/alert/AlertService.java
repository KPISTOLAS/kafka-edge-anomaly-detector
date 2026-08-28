package com.example.projecte.alert;

import com.example.projecte.model.Anomaly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    /**
     * Runs off the Kafka consumer thread so detection stays unblocked.
     * Swap this body for an email / Slack / webhook client when wiring a real channel.
     */
    @Async
    public void sendAlert(Anomaly anomaly) {
        log.warn("ALERT device={} metric={} value={} z={} at {}",
                anomaly.deviceId(),
                anomaly.metric(),
                String.format("%.2f", anomaly.value()),
                String.format("%.2f", anomaly.zScore()),
                anomaly.detectedAt());
    }
}
