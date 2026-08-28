package com.example.projecte.report;

import com.example.projecte.detect.AnomalyBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReportScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduler.class);

    private final AnomalyBuffer buffer;

    public ReportScheduler(AnomalyBuffer buffer) {
        this.buffer = buffer;
    }

    @Scheduled(fixedRate = 30_000)
    public void report() {
        log.info("Rolling report - {} anomalies in buffer, by device: {}",
                buffer.size(), buffer.countsByDevice());
    }
}
