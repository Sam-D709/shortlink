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
     * 定时任务,整理月度统计数据,包括：访问量,访客数等，并保存到数据库中
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void statMonthJob() {
        stateMonthStatService.statAndSaveLastMonth();
    }
}

