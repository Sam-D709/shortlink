package org.samd.shortlink.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StateMonthStatScheduler {
    private final StateMonthStatService stateMonthStatService;

    /**
     * 每月1日凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void statMonthJob() {
        stateMonthStatService.statAndSaveLastMonth();
    }
}

