package org.samd.shortlink.project.dto.resp;

import lombok.Data;

import java.util.Map;

@Data
public class LinkDayStateRespDTO {
    /**
     * 日期访问统计，key为yyyy-MM-dd，value为{pv, uv}
     */
    private Map<String, Map<String, Integer>> dayPvUv;
    /**
     * 每日浏览器统计，key为日期，value为{浏览器类型: {count, percent}}
     */
    private Map<String, Map<String, Map<String, Object>>> browserStatMap;
    /**
     * 每日设备统计，key为日期，value为{设备类型: {count, percent}}
     */
    private Map<String, Map<String, Map<String, Object>>> deviceStatMap;
    /**
     * 每日操作系统统计，key为日期，value为{操作系统类型: {count, percent}}
     */
    private Map<String, Map<String, Map<String, Object>>> osStatMap;
}
