package org.samd.shortlink.project.mq.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.samd.shortlink.project.dao.entity.*;
import org.samd.shortlink.project.dao.mapper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 统计聚合以及刷盘
 * 满100发起数据库更新,或者每5秒强制更新一次
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StatsAggregator {

    private final List<AccessStateHourDO> hourBuffer = new ArrayList<>();
    private final List<AccessStateDayDO> dayBuffer = new ArrayList<>();
    private final List<AccessStateMonthDO> monthBuffer = new ArrayList<>();
    private final List<OSStateDO> osBuffer = new ArrayList<>();
    private final List<BrowserStateDO> browserBuffer = new ArrayList<>();
    private final List<DeviceStateDO> deviceBuffer = new ArrayList<>();

    private final AccessStateHourMapper hourMapper;
    private final AccessStateDayMapper dayMapper;
    private final AccessStateMonthMapper monthMapper;
    private final OSStateMapper osMapper;
    private final BrowserStateMapper browserMapper;
    private final DeviceStateMapper deviceMapper;

    @Value("${mq.consumer.batch-size:100}")
    private int batchSize;

    /**
     * 聚合小时数据
     */
    public synchronized void aggregateHour(AccessStateHourDO data) {
        // 查找是否已有相同维度（同链接+同小时）
        Optional<AccessStateHourDO> existing = hourBuffer.stream()
                .filter(h -> h.getFullshorturl().equals(data.getFullshorturl())
                        && h.getHour().equals(data.getHour())
                        && isSameDay(h.getDate(), data.getDate()))
                .findFirst();

        if (existing.isPresent()) {
            AccessStateHourDO h = existing.get();
            h.setPv(h.getPv() + data.getPv());
            h.setUv(h.getUv() + data.getUv());
        } else {
            hourBuffer.add(data);
        }

        // 达到批次阈值，刷盘
        if (hourBuffer.size() >= batchSize) {
            flushHour();
        }
    }

    /**
     * 聚合日数据
     */
    public synchronized void aggregateDay(AccessStateDayDO data) {
        Optional<AccessStateDayDO> existing = dayBuffer.stream()
                .filter(d -> d.getFullshorturl().equals(data.getFullshorturl())
                        && isSameDay(d.getDate(), data.getDate()))
                .findFirst();

        if (existing.isPresent()) {
            AccessStateDayDO d = existing.get();
            d.setPv(d.getPv() + data.getPv());
            d.setUv(d.getUv() + data.getUv());
        } else {
            dayBuffer.add(data);
        }

        if (dayBuffer.size() >= batchSize) flushDay();
    }

    /**
     * 聚合月数据
     */
    public synchronized void aggregateMonth(AccessStateMonthDO data) {
        Optional<AccessStateMonthDO> existing = monthBuffer.stream()
                .filter(m -> m.getFullshorturl().equals(data.getFullshorturl())
                        && m.getYear().equals(data.getYear())
                        && m.getMonth().equals(data.getMonth()))
                .findFirst();

        if (existing.isPresent()) {
            AccessStateMonthDO m = existing.get();
            m.setPv(m.getPv() + data.getPv());
            m.setUv(m.getUv() + data.getUv());
        } else {
            monthBuffer.add(data);
        }

        if (monthBuffer.size() >= batchSize) flushMonth();
    }

    /**
     * 聚合OS数据
     */
    public synchronized void aggregateOS(OSStateDO data) {
        Optional<OSStateDO> existing = osBuffer.stream()
                .filter(o -> o.getFullshorturl().equals(data.getFullshorturl())
                        && o.getOs().equals(data.getOs())
                        && isSameDay(o.getDate(), data.getDate()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setCnt(existing.get().getCnt() + data.getCnt());
        } else {
            osBuffer.add(data);
        }

        if (osBuffer.size() >= batchSize) flushOS();
    }

    /**
     * 聚合Browser数据
     */
    public synchronized void aggregateBrowser(BrowserStateDO data) {
        Optional<BrowserStateDO> existing = browserBuffer.stream()
                .filter(b -> b.getFullshorturl().equals(data.getFullshorturl())
                        && b.getBrowser().equals(data.getBrowser())
                        && isSameDay(b.getDate(), data.getDate()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setCnt(existing.get().getCnt() + data.getCnt());
        } else {
            browserBuffer.add(data);
        }

        if (browserBuffer.size() >= batchSize) flushBrowser();
    }

    /**
     * 聚合Device数据
     */
    public synchronized void aggregateDevice(DeviceStateDO data) {
        Optional<DeviceStateDO> existing = deviceBuffer.stream()
                .filter(d -> d.getFullshorturl().equals(data.getFullshorturl())
                        && d.getDevice().equals(data.getDevice())
                        && isSameDay(d.getDate(), data.getDate()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setCnt(existing.get().getCnt() + data.getCnt());
        } else {
            deviceBuffer.add(data);
        }

        if (deviceBuffer.size() >= batchSize) flushDevice();
    }

    public synchronized void flushHour() {
        if (hourBuffer.isEmpty()) return;
        try {
            hourMapper.batchInsertOrUpdate(new ArrayList<>(hourBuffer));
            log.debug("[消息队列DO]刷盘HOUR: {}条", hourBuffer.size());
            hourBuffer.clear();
        } catch (Exception e) {
            log.error("[消息队列DO]刷盘HOUR失败: {}", e.getMessage());
            throw new RuntimeException("[消息队列DO]刷盘失败", e);
        }
    }

    public synchronized void flushDay() {
        if (dayBuffer.isEmpty()) return;
        try {
            dayMapper.batchInsertOrUpdate(dayBuffer);
            log.debug("[消息队列DO]刷盘DAY: {}条", dayBuffer.size());
            dayBuffer.clear();
        } catch (Exception e) {
            log.error("[消息队列DO]刷盘DAY失败: {}", e.getMessage());
            throw new RuntimeException("[消息队列DO]刷盘失败", e);
        }
    }

    public synchronized void flushMonth() {
        if (monthBuffer.isEmpty()) return;
        try {
            monthMapper.batchInsertOrUpdate(monthBuffer);
            log.debug("[消息队列DO]刷盘MONTH: {}条", monthBuffer.size());
            monthBuffer.clear();
        } catch (Exception e) {
            log.error("[消息队列DO]刷盘MONTH失败:{}", e.getMessage());
            throw new RuntimeException("[消息队列DO]刷盘失败", e);
        }
    }

    public synchronized void flushOS() {
        if (osBuffer.isEmpty()) return;
        try {
            osMapper.batchInsertOrUpdate(osBuffer);
            log.debug("[消息队列DO]刷盘OS: {}条", osBuffer.size());
            osBuffer.clear();
        } catch (Exception e) {
            log.error("[消息队列DO]刷盘OS失败: {}", e.getMessage());
            throw new RuntimeException("[消息队列DO]刷盘失败", e);
        }
    }

    public synchronized void flushBrowser() {
        if (browserBuffer.isEmpty()) return;
        try {
            browserMapper.batchInsertOrUpdate(browserBuffer);
            log.debug("[消息队列DO]刷盘BROWSER: {}条", browserBuffer.size());
            browserBuffer.clear();
        } catch (Exception e) {
            log.error("[消息队列DO]刷盘BROWSER失败: {}", e.getMessage());
            throw new RuntimeException("[消息队列DO]刷盘失败", e);
        }
    }

    public synchronized void flushDevice() {
        if (deviceBuffer.isEmpty()) return;
        try {
            deviceMapper.batchInsertOrUpdate(deviceBuffer);
            log.debug("[消息队列DO]刷盘DEVICE: {}条", deviceBuffer.size());
            deviceBuffer.clear();
        } catch (Exception e) {
            log.error("[消息队列DO]刷盘DEVICE失败: {}", e.getMessage());
            throw new RuntimeException("[消息队列DO]刷盘失败", e);
        }
    }

    /**
     * 强制刷盘所有
     */
    public synchronized void flushAll() {
        if (!hasData()) return;
        log.debug("[消息队列DO]定时刷盘触发...");
        flushHour();
        flushDay();
        flushMonth();
        flushOS();
        flushBrowser();
        flushDevice();
    }

    /**
     * 检查是否有待刷盘数据
     */
    public boolean hasData() {
        return !hourBuffer.isEmpty() || !dayBuffer.isEmpty() ||
                !monthBuffer.isEmpty() || !osBuffer.isEmpty() ||
                !browserBuffer.isEmpty() || !deviceBuffer.isEmpty();
    }
    // ==================== 工具方法 ====================

    private boolean isSameDay(Date d1, Date d2) {
        return DateUtils.isSameDay(d1, d2);
    }
}