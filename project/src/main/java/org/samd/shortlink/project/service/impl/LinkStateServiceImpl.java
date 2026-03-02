package org.samd.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.samd.shortlink.project.dao.entity.*;
import org.samd.shortlink.project.dao.mapper.*;
import org.samd.shortlink.project.dto.resp.LinkDayStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkDefaultStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkMonthStateRespDTO;
import org.samd.shortlink.project.service.LinkStateService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkStateServiceImpl implements LinkStateService {
    private final AccessStateHourMapper accessStateHourMapper;
    private final BrowserStateMapper browserStateMapper;
    private final DeviceStateMapper deviceStateMapper;
    private final OSStateMapper osStateMapper;
    private final AccessStateDayMapper accessStateDayMapper;
    private final AccessStateMonthMapper accessStateMonthMapper;
    private final OSStateMonthMapper osStateMonthMapper;
    private final DeviceStateMonthMapper deviceStateMonthMapper;
    private final BrowserStateMonthMapper browserStateMonthMapper;

    @Override
    public LinkDefaultStateRespDTO getDefaultLinkState(String fullshorturl) {
        LinkDefaultStateRespDTO respDTO = new LinkDefaultStateRespDTO();
        Map<Integer, Map<String, Integer>> hourPvUv = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        // 过去24小时
        for (int i = 0; i < 24; i++) {
            LocalDateTime hourTime = now.minusHours(23 - i);
            int hour = hourTime.getHour();
            LocalDate dateOnly = hourTime.toLocalDate();
            Date date = Date.from(dateOnly.atStartOfDay(ZoneId.systemDefault()).toInstant());
            List<AccessStateHourDO> list = accessStateHourMapper.selectList(
                new QueryWrapper<AccessStateHourDO>()
                    .eq("fullshorturl", fullshorturl)
                    .eq("hour", hour)
                    .eq("delflag", 0)
                    .eq("date", date)
            );
            int pv = 0, uv = 0;
            if (!list.isEmpty()) {
                pv = list.stream().mapToInt(AccessStateHourDO::getPv).sum();
                uv = list.stream().mapToInt(AccessStateHourDO::getUv).sum();
            }
            Map<String, Integer> pvUvMap = new HashMap<>();
            pvUvMap.put("pv", pv);
            pvUvMap.put("uv", uv);
            hourPvUv.put(hour, pvUvMap);
        }
        respDTO.setHourPvUv(hourPvUv);

        // 今天日期
        LocalDate today = LocalDate.now();
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 浏览器统计
        List<BrowserStateDO> browserList = browserStateMapper.selectList(
            new QueryWrapper<BrowserStateDO>()
                .eq("fullshorturl", fullshorturl)
                .eq("date", todayDate)
                .eq("delflag", 0)
        );
        int browserTotal = browserList.stream().mapToInt(BrowserStateDO::getCnt).sum();
        Map<String, Map<String, Object>> browserStat = new HashMap<>();
        for (BrowserStateDO b : browserList) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("count", b.getCnt());
            stat.put("percent", browserTotal == 0 ? 0.0 : (b.getCnt() * 100.0 / browserTotal));
            browserStat.put(b.getBrowser(), stat);
        }
        respDTO.setBrowserStat(browserStat);

        // 设备统计
        List<DeviceStateDO> deviceList = deviceStateMapper.selectList(
            new QueryWrapper<DeviceStateDO>()
                .eq("fullshorturl", fullshorturl)
                .eq("date", todayDate)
                .eq("delflag", 0)
        );
        int deviceTotal = deviceList.stream().mapToInt(DeviceStateDO::getCnt).sum();
        Map<String, Map<String, Object>> deviceStat = new HashMap<>();
        for (DeviceStateDO d : deviceList) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("count", d.getCnt());
            stat.put("percent", deviceTotal == 0 ? 0.0 : (d.getCnt() * 100.0 / deviceTotal));
            deviceStat.put(d.getDevice(), stat);
        }
        respDTO.setDeviceStat(deviceStat);

        // 操作系统统计
        List<OSStateDO> osList = osStateMapper.selectList(
            new QueryWrapper<OSStateDO>()
                .eq("fullshorturl", fullshorturl)
                .eq("date", todayDate)
                .eq("delflag", 0)
        );
        int osTotal = osList.stream().mapToInt(OSStateDO::getCnt).sum();
        Map<String, Map<String, Object>> osStat = new HashMap<>();
        for (OSStateDO o : osList) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("count", o.getCnt());
            stat.put("percent", osTotal == 0 ? 0.0 : (o.getCnt() * 100.0 / osTotal));
            osStat.put(o.getOs(), stat);
        }
        respDTO.setOsStat(osStat);

        return respDTO;
    }

    @Override
    public LinkDayStateRespDTO getDayLinkState(String fullshorturl, String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        QueryWrapper<AccessStateDayDO> wrapper = new QueryWrapper<>();
        wrapper.eq("fullshorturl", fullshorturl)
                .ge("date", startDate)
                .le("date", endDate)
                .eq("delflag", 0);
        List<AccessStateDayDO> dayList = accessStateDayMapper.selectList(wrapper);
        Map<String, AccessStateDayDO> dayMap = new HashMap<>();
        for (AccessStateDayDO dayDO : dayList) {
            String dateKey = sdf.format(dayDO.getDate());
            dayMap.put(dateKey, dayDO);
        }
        LinkDayStateRespDTO respDTO = new LinkDayStateRespDTO();
        Map<String, Map<String, Integer>> dayPvUv = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, Object>>> browserStatMap = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, Object>>> deviceStatMap = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, Object>>> osStatMap = new LinkedHashMap<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String dateKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            AccessStateDayDO dayDO = dayMap.get(dateKey);
            Map<String, Integer> pvUvMap = new HashMap<>();
            pvUvMap.put("pv", dayDO != null ? dayDO.getPv() : 0);
            pvUvMap.put("uv", dayDO != null ? dayDO.getUv() : 0);
            dayPvUv.put(dateKey, pvUvMap);
            // 浏览器统计
            List<BrowserStateDO> browserList = dayDO != null ? browserStateMapper.selectList(
                new QueryWrapper<BrowserStateDO>()
                    .eq("fullshorturl", fullshorturl)
                    .eq("date", dayDO.getDate())
                    .eq("delflag", 0)
            ) : Collections.emptyList();
            int browserTotal = browserList.stream().mapToInt(BrowserStateDO::getCnt).sum();
            Map<String, Map<String, Object>> browserStat = new HashMap<>();
            if (browserList.isEmpty()) {
                browserStat.put("无数据", Map.of("count", 0, "percent", "0%"));
            } else {
                for (BrowserStateDO b : browserList) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("count", b.getCnt());
                    stat.put("percent", browserTotal == 0 ? "0%" : String.format("%.2f%%", b.getCnt() * 100.0 / browserTotal));
                    browserStat.put(b.getBrowser(), stat);
                }
            }
            browserStatMap.put(dateKey, browserStat);
            // 设备统计
            List<DeviceStateDO> deviceList = dayDO != null ? deviceStateMapper.selectList(
                new QueryWrapper<DeviceStateDO>()
                    .eq("fullshorturl", fullshorturl)
                    .eq("date", dayDO.getDate())
                    .eq("delflag", 0)
            ) : Collections.emptyList();
            int deviceTotal = deviceList.stream().mapToInt(DeviceStateDO::getCnt).sum();
            Map<String, Map<String, Object>> deviceStat = new HashMap<>();
            if (deviceList.isEmpty()) {
                deviceStat.put("无数据", Map.of("count", 0, "percent", "0%"));
            } else {
                for (DeviceStateDO d : deviceList) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("count", d.getCnt());
                    stat.put("percent", deviceTotal == 0 ? "0%" : String.format("%.2f%%", d.getCnt() * 100.0 / deviceTotal));
                    deviceStat.put(d.getDevice(), stat);
                }
            }
            deviceStatMap.put(dateKey, deviceStat);
            // 操作系统统计
            List<OSStateDO> osList = dayDO != null ? osStateMapper.selectList(
                new QueryWrapper<OSStateDO>()
                    .eq("fullshorturl", fullshorturl)
                    .eq("date", dayDO.getDate())
                    .eq("delflag", 0)
            ) : Collections.emptyList();
            int osTotal = osList.stream().mapToInt(OSStateDO::getCnt).sum();
            Map<String, Map<String, Object>> osStat = new HashMap<>();
            if (osList.isEmpty()) {
                osStat.put("无数据", Map.of("count", 0, "percent", "0%"));
            } else {
                for (OSStateDO o : osList) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("count", o.getCnt());
                    stat.put("percent", osTotal == 0 ? "0%" : String.format("%.2f%%", o.getCnt() * 100.0 / osTotal));
                    osStat.put(o.getOs(), stat);
                }
            }
            osStatMap.put(dateKey, osStat);
        }
        respDTO.setDayPvUv(dayPvUv);
        respDTO.setBrowserStatMap(browserStatMap);
        respDTO.setDeviceStatMap(deviceStatMap);
        respDTO.setOsStatMap(osStatMap);
        return respDTO;
    }

    @Override
    public LinkMonthStateRespDTO getMonthLinkState(String fullshorturl, String startMonth, String endMonth) {
        int startYear = Integer.parseInt(startMonth.substring(0, 4));
        int startMonthInt = Integer.parseInt(startMonth.substring(5, 7));
        int endYear = Integer.parseInt(endMonth.substring(0, 4));
        int endMonthInt = Integer.parseInt(endMonth.substring(5, 7));
        QueryWrapper<AccessStateMonthDO> wrapper = new QueryWrapper<>();
        wrapper.eq("fullshorturl", fullshorturl)
                .ge("year", startYear)
                .ge("month", startMonthInt)
                .le("year", endYear)
                .le("month", endMonthInt)
                .eq("delflag", 0);
        List<AccessStateMonthDO> monthList = accessStateMonthMapper.selectList(wrapper);
        Map<String, AccessStateMonthDO> monthMap = new HashMap<>();
        for (AccessStateMonthDO monthDO : monthList) {
            String key = monthDO.getYear() + "-" + String.format("%02d", monthDO.getMonth());
            monthMap.put(key, monthDO);
        }
        LinkMonthStateRespDTO respDTO = new LinkMonthStateRespDTO();
        Map<String, Map<String, Integer>> monthPvUv = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, Object>>> osMonthStat = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, Object>>> deviceMonthStat = new LinkedHashMap<>();
        Map<String, Map<String, Map<String, Object>>> browserMonthStat = new LinkedHashMap<>();
        int y = startYear, m = startMonthInt;
        while (y < endYear || (y == endYear && m <= endMonthInt)) {
            String key = y + "-" + String.format("%02d", m);
            AccessStateMonthDO monthDO = monthMap.get(key);
            Map<String, Integer> pvUvMap = new HashMap<>();
            pvUvMap.put("pv", monthDO != null ? monthDO.getPv() : 0);
            pvUvMap.put("uv", monthDO != null ? monthDO.getUv() : 0);
            monthPvUv.put(key, pvUvMap);
            // osMonthStat
            List<OSStateMonthDO> osList = monthDO != null ? osStateMonthMapper.selectList(
                new QueryWrapper<OSStateMonthDO>()
                    .eq("fullshorturl", fullshorturl)
                    .eq("year", y)
                    .eq("month", m)
            ) : Collections.emptyList();
            int osTotal = osList.stream().mapToInt(OSStateMonthDO::getCnt).sum();
            Map<String, Map<String, Object>> osStat = new HashMap<>();
            if (osList.isEmpty()) {
                osStat.put("无数据", Map.of("count", 0, "percent", "0%"));
            } else {
                for (OSStateMonthDO o : osList) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("count", o.getCnt());
                    stat.put("percent", osTotal == 0 ? "0%" : String.format("%.2f%%", o.getCnt() * 100.0 / osTotal));
                    osStat.put(o.getOs(), stat);
                }
            }
            osMonthStat.put(key, osStat);
            // deviceMonthStat
            List<DeviceStateMonthDO> deviceList = monthDO != null ? deviceStateMonthMapper.selectList(
                new QueryWrapper<DeviceStateMonthDO>()
                    .eq("fullshorturl", fullshorturl)
                    .eq("year", y)
                    .eq("month", m)
            ) : Collections.emptyList();
            int deviceTotal = deviceList.stream().mapToInt(DeviceStateMonthDO::getCnt).sum();
            Map<String, Map<String, Object>> deviceStat = new HashMap<>();
            if (deviceList.isEmpty()) {
                deviceStat.put("无数据", Map.of("count", 0, "percent", "0%"));
            } else {
                for (DeviceStateMonthDO d : deviceList) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("count", d.getCnt());
                    stat.put("percent", deviceTotal == 0 ? "0%" : String.format("%.2f%%", d.getCnt() * 100.0 / deviceTotal));
                    deviceStat.put(d.getDevice(), stat);
                }
            }
            deviceMonthStat.put(key, deviceStat);
            // browserMonthStat
            List<BrowserStateMonthDO> browserList = monthDO != null ? browserStateMonthMapper.selectList(
                new QueryWrapper<BrowserStateMonthDO>()
                    .eq("fullshorturl", fullshorturl)
                    .eq("year", y)
                    .eq("month", m)
            ) : Collections.emptyList();
            int browserTotal = browserList.stream().mapToInt(BrowserStateMonthDO::getCnt).sum();
            Map<String, Map<String, Object>> browserStat = new HashMap<>();
            if (browserList.isEmpty()) {
                browserStat.put("无数据", Map.of("count", 0, "percent", "0%"));
            } else {
                for (BrowserStateMonthDO b : browserList) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("count", b.getCnt());
                    stat.put("percent", browserTotal == 0 ? "0%" : String.format("%.2f%%", b.getCnt() * 100.0 / browserTotal));
                    browserStat.put(b.getBrowser(), stat);
                }
            }
            browserMonthStat.put(key, browserStat);
            // 下一个月
            m++;
            if (m > 12) {
                m = 1;
                y++;
            }
        }
        respDTO.setMonthPvUv(monthPvUv);
        respDTO.setOsMonthStat(osMonthStat);
        respDTO.setDeviceMonthStat(deviceMonthStat);
        respDTO.setBrowserMonthStat(browserMonthStat);
        return respDTO;
    }
}
