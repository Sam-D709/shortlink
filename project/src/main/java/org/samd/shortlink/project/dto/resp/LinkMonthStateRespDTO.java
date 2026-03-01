package org.samd.shortlink.project.dto.resp;

import lombok.Data;

import java.util.Map;

@Data
public class LinkMonthStateRespDTO {
    /**
     * 月份访问统计，key为yyyy-MM，value为{pv, uv}
     */
    private Map<String, Map<String, Integer>> monthPvUv;
    /**
     * 每月操作系统统计，key为yyyy-MM，value为{os: {count, percent}}
     */
    private Map<String, Map<String, Map<String, Object>>> osMonthStat;
    /**
     * 每月设备统计，key为yyyy-MM，value为{device: {count, percent}}
     */
    private Map<String, Map<String, Map<String, Object>>> deviceMonthStat;
    /**
     * 每月浏览器统计，key为yyyy-MM，value为{browser: {count, percent}}
     */
    private Map<String, Map<String, Map<String, Object>>> browserMonthStat;
}
