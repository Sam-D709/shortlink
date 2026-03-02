package org.samd.shortlink.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UvStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 小时内首次访问（true=首次，false=非首次）
     */
    private Boolean uvFirstFlag;

    /**
     * 天内首次访问（true=首次，false=非首次）
     */
    private Boolean uvDayFirstFlag;

    /**
     * 月内首次访问（true=首次，false=非首次）
     */
    private Boolean uvMonthFirstFlag;

    /**
     * 短链接标识
     */
    private String shortLink;

    /**
     * UV 唯一标识值（如果是首次访问，会生成新的UUID）
     */
    private String uvValue;

    // ==================== 请求信息 ====================

    /**
     * 请求协议（http/https）
     */
    private String protocol;

    /**
     * 请求域名（Host，去除端口号）
     */
    private String domain;

    /**
     * 完整请求来源
     */
    private String origin;

    // ==================== 新增：客户端信息（User-Agent解析） ====================

    /**
     * 原始 User-Agent 字符串
     */
    private String userAgent;

    /**
     * 浏览器名称（如 Chrome, Firefox, Safari, Edge）
     */
    private String browser;

    /**
     * 浏览器版本
     */
    private String browserVersion;

    /**
     * 操作系统名称（如 Windows, macOS, Linux, Android, iOS）
     */
    private String os;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * 设备类型（Desktop, Mobile, Tablet）
     */
    private String deviceType;

    /**
     * 设备厂商/品牌（如 Apple, Samsung, Huawei）
     */
    private String deviceBrand;

    /**
     * 渲染引擎（如 WebKit, Gecko, Blink）
     */
    private String engine;

    /**
     * 是否移动端
     */
    private Boolean mobile;

    /**
     * 客户端 IP 地址
     */
    private String clientIp;
}