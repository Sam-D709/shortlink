package org.samd.shortlink.project.common.util;

import jakarta.servlet.http.HttpServletRequest;

public final class LinkMonitorUtil {
    private LinkMonitorUtil() {}

    /**
     * 从HttpServletRequest中获取操作系统名称
     * @param request HTTP请求对象
     * @return 操作系统名称，如 Windows、Mac OS、Linux、Android、iOS 等
     */
    public static String getOSFromRequest(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }
        String userAgent = request.getHeader("User-Agent");
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
}
