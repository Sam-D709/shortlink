package org.samd.shortlink.project.mq.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatsFlushJob {

    private final StatsAggregator aggregator;

    public StatsFlushJob(StatsAggregator aggregator) {
        this.aggregator = aggregator;
    }

    /**
     * 每5秒强制刷盘一次
     */
    @Scheduled(fixedRate = 5000)
    public void scheduledFlush() {
        aggregator.flushAll();
    }
}
