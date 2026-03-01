package org.samd.shortlink.project.dto.resp;

import lombok.Data;

import java.util.Map;

@Data
public class LinkDefaultStateRespDTO {
    /**
     * 小时访问统计，key为小时，value为{pv, uv}
     */
    private Map<Integer, Map<String, Integer>> hourPvUv;

    /**
     * 浏览器统计，key为浏览器类型，value为{count, percent}
     */
    private Map<String, Map<String, Object>> browserStat;
    /**
     * 设备统计，key为设备类型，value为{count, percent}
     */
    private Map<String, Map<String, Object>> deviceStat;
    /**
     * 操作系统统计，key为操作系统类型，value为{count, percent}
     */
    private Map<String, Map<String, Object>> osStat;
}
