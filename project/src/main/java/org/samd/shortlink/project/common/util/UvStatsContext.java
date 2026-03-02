package org.samd.shortlink.project.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import org.samd.shortlink.project.common.conversion.exception.ClientException;
import org.samd.shortlink.project.dto.req.UvStatsDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * UV 统计上下文工具类
 * 用于在下游服务任意地方获取 UV 统计信息、请求信息和客户端信息
 */
@Component
public class UvStatsContext {

    /**
     * 请求头中的 UV 统计信息 Key
     */
    private static final String UV_STATS_HEADER = "X-UV-Stats";

    /**
     * 获取当前请求对象
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * 获取 UV 统计信息
     *
     * @return UvStatsDTO，如果不存在返回 null
     */
    public static UvStatsDTO getUvStats() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String uvStatsJson = request.getHeader(UV_STATS_HEADER);
        if (StrUtil.isBlank(uvStatsJson)) {
            return null;
        }

        try {
            return JSON.parseObject(uvStatsJson, UvStatsDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取 UV 统计信息，如果不存在则抛出异常
     */
    public static UvStatsDTO getUvStatsOrThrow() {
        UvStatsDTO stats = getUvStats();
        if (stats == null) {
            throw new ClientException("UV 统计信息不存在");
        }
        return stats;
    }

    // ==================== UV 标志位方法 ====================

    public static boolean isUvFirst() {
        UvStatsDTO stats = getUvStats();
        return stats != null && Boolean.TRUE.equals(stats.getUvFirstFlag());
    }

    public static boolean isUvDayFirst() {
        UvStatsDTO stats = getUvStats();
        return stats != null && Boolean.TRUE.equals(stats.getUvDayFirstFlag());
    }

    public static boolean isUvMonthFirst() {
        UvStatsDTO stats = getUvStats();
        return stats != null && Boolean.TRUE.equals(stats.getUvMonthFirstFlag());
    }

    public static String getUvValue() {
        UvStatsDTO stats = getUvStats();
        return stats != null ? stats.getUvValue() : null;
    }

    public static String getShortLink() {
        UvStatsDTO stats = getUvStats();
        return stats != null ? stats.getShortLink() : null;
    }

    // ==================== 请求信息方法 ====================

    public static String getProtocol() {
        UvStatsDTO stats = getUvStats();
        if (stats != null && StrUtil.isNotBlank(stats.getProtocol())) {
            return stats.getProtocol();
        }

        HttpServletRequest request = getRequest();
        if (request == null) {
            return "http";
        }

        String protocol = request.getHeader("X-Forwarded-Proto");
        if (StrUtil.isBlank(protocol)) {
            protocol = request.getHeader("X-Scheme");
        }
        if (StrUtil.isBlank(protocol)) {
            protocol = request.getScheme();
        }

        return protocol;
    }

    public static String getDomain() {
        UvStatsDTO stats = getUvStats();
        if (stats != null && StrUtil.isNotBlank(stats.getDomain())) {
            return stats.getDomain();
        }

        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String host = request.getHeader("Host");
        if (StrUtil.isBlank(host)) {
            host = request.getServerName();
        }

        if (host != null && host.contains(":")) {
            host = host.split(":")[0];
        }

        return host;
    }

    public static String getOrigin() {
        UvStatsDTO stats = getUvStats();
        if (stats != null && StrUtil.isNotBlank(stats.getOrigin())) {
            return stats.getOrigin();
        }

        String protocol = getProtocol();
        String domain = getDomain();

        if (StrUtil.isAllBlank(protocol, domain)) {
            return null;
        }

        return protocol + "://" + domain;
    }

    // ==================== 新增：客户端信息方法（User-Agent） ====================

    /**
     * 获取原始 User-Agent 字符串
     */
    public static String getUserAgentString() {
        UvStatsDTO stats = getUvStats();
        if (stats != null && StrUtil.isNotBlank(stats.getUserAgent())) {
            return stats.getUserAgent();
        }

        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        return request.getHeader("User-Agent");
    }

    /**
     * 获取浏览器名称（如 Chrome, Firefox, Safari）
     */
    public static String getBrowser() {
        UvStatsDTO stats = getUvStats();
        if (stats != null && StrUtil.isNotBlank(stats.getBrowser())) {
            return stats.getBrowser();
        }

        String userAgent = getUserAgentString();
        if (StrUtil.isBlank(userAgent)) {
            return "Unknown";
        }

        UserAgent ua = UserAgentUtil.parse(userAgent);
        return ua.getBrowser().getName();
    }

    /**
     * 获取操作系统名称（如 Windows, macOS, Android, iOS）
     */
    public static String getOs() {
        String userAgent = getUserAgentString();
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows")) {
            return "Windows";
        } else if (ua.contains("mac os x") || ua.contains("macintosh")) {
            return "Mac OS";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) {
            return "iOS";
        } else if (ua.contains("linux")) {
            return "Linux";
        } else if (ua.contains("unix")) {
            return "Unix";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取设备厂商/品牌
     */
    public static String getDevice() {
        String userAgent = getUserAgentString();
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg")) {
            return "Edge";
        } else if (ua.contains("opr") || ua.contains("opera")) {
            return "Opera";
        } else if (ua.contains("chrome")) {
            return "Chrome";
        } else if (ua.contains("firefox")) {
            return "Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        } else if (ua.contains("msie") || ua.contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }
}