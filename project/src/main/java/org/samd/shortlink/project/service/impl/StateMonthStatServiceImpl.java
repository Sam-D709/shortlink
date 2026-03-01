package org.samd.shortlink.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.samd.shortlink.project.dao.entity.BrowserStateMonthDO;
import org.samd.shortlink.project.dao.entity.DeviceStateMonthDO;
import org.samd.shortlink.project.dao.entity.OSStateMonthDO;
import org.samd.shortlink.project.dao.mapper.*;
import org.samd.shortlink.project.service.StateMonthStatService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StateMonthStatServiceImpl implements StateMonthStatService {
    private final BrowserStateMonthMapper browserStateMonthMapper;
    private final DeviceStateMonthMapper deviceStateMonthMapper;
    private final OSStateMonthMapper osStateMonthMapper;
    private final BrowserStateMapperExt browserStateMapperExt;
    private final DeviceStateMapperExt deviceStateMapperExt;
    private final OSStateMapperExt osStateMapperExt;

    @Override
    public void statAndSaveLastMonth() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate start = lastMonth.atDay(1);
        LocalDate end = lastMonth.plusMonths(1).atDay(1);
        // 统计 OS
        List<OSStateMonthDO> osMonthStats = osStateMapperExt.selectMonthStat(start, end);
        if (!osMonthStats.isEmpty()) {
            osStateMonthMapper.batchInsert(osMonthStats);
        }
        // 统计 Browser
        List<BrowserStateMonthDO> browserMonthStats = browserStateMapperExt.selectMonthStat(start, end);
        if (!browserMonthStats.isEmpty()) {
            browserStateMonthMapper.batchInsert(browserMonthStats);
        }
        // 统计 Device
        List<DeviceStateMonthDO> deviceMonthStats = deviceStateMapperExt.selectMonthStat(start, end);
        if (!deviceMonthStats.isEmpty()) {
            deviceStateMonthMapper.batchInsert(deviceMonthStats);
        }
    }
}
