package org.samd.shortlink.project;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;

import java.time.LocalDateTime;
import java.util.Date;

public class ShortLinkRoutingTest {

    public static void main(String[] args) {
        int hour = LocalDateTime.now().getHour();
        Week week = DateUtil.dayOfWeekEnum(new Date());
        int weekday = week.getValue();

        System.out.println(hour + " " + weekday);
        // 确认每次运行结果一致
    }
}
