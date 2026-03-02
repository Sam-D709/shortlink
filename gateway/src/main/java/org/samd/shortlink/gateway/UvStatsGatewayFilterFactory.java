package org.samd.shortlink.gateway;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * UV 统计 Cookie 处理过滤器
 * 专门处理短链接访问的 UV 统计逻辑
 * 处理 Cookie、设置 UV 标志头，同时传递请求协议、域名和客户端信息
 */
@Slf4j
@Component
public class UvStatsGatewayFilterFactory extends AbstractGatewayFilterFactory<UvStatsGatewayFilterFactory.Config> {

    // Cookie 名称前缀
    private static final String COOKIE_PREFIX_HOUR = "sl_state_hour_";
    private static final String COOKIE_PREFIX_DAY = "sl_state_day_";
    private static final String COOKIE_PREFIX_MONTH = "sl_state_month_";

    // 传递给下游服务的请求头
    private static final String UV_STATS_HEADER = "X-UV-Stats";

    // 反向代理相关头
    private static final String HEADER_X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String HEADER_X_SCHEME = "X-Scheme";
    private static final String HEADER_HOST = "Host";
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_X_REAL_IP = "X-Real-IP";
    private static final String HEADER_USER_AGENT = "User-Agent";

    public UvStatsGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 获取短链接标识
            String path = request.getURI().getPath();
            String shortLink = path.substring(1);

            String protocol = getProtocol(request);
            String domain = getDomain(request);
            String origin = protocol + "://" + domain;

            String userAgentString = getHeader(request, HEADER_USER_AGENT);
            UserAgentParseResult clientInfo = parseUserAgent(userAgentString);

            String clientIp = getClientIp(request);

            log.debug("短链接 [{}] 请求信息 - 协议: [{}], 域名: [{}], IP: [{}], UA: [{}]",
                    shortLink, protocol, domain, clientIp,
                    StrUtil.sub(userAgentString, 0, 50));

            boolean uvFirstFlag = true;
            boolean uvDayFirstFlag = true;
            boolean uvMonthFirstFlag = true;

            MultiValueMap<String, HttpCookie> cookies = request.getCookies();

            if (!CollectionUtils.isEmpty(cookies)) {
                List<HttpCookie> hourCookies = cookies.get(COOKIE_PREFIX_HOUR + shortLink);
                if (!CollectionUtils.isEmpty(hourCookies)) {
                    uvFirstFlag = false;
                }

                List<HttpCookie> dayCookies = cookies.get(COOKIE_PREFIX_DAY + shortLink);
                if (!CollectionUtils.isEmpty(dayCookies)) {
                    uvDayFirstFlag = false;
                }

                List<HttpCookie> monthCookies = cookies.get(COOKIE_PREFIX_MONTH + shortLink);
                if (!CollectionUtils.isEmpty(monthCookies)) {
                    uvMonthFirstFlag = false;
                }
            }

            // 生成 UV 值
            String uvValue = IdUtil.fastUUID();
            LocalDateTime now = LocalDateTime.now();

            // 设置新的 Cookie
            try {
                if (uvFirstFlag) {
                    ResponseCookie hourCookie = buildCookie(COOKIE_PREFIX_HOUR + shortLink, uvValue,
                            "/" + shortLink, calculateSecondsToNextHour(now), protocol);
                    response.addCookie(hourCookie);
                }

                if (uvDayFirstFlag) {
                    ResponseCookie dayCookie = buildCookie(COOKIE_PREFIX_DAY + shortLink, uvValue,
                            "/" + shortLink, calculateSecondsToNextDay(now), protocol);
                    response.addCookie(dayCookie);
                }

                if (uvMonthFirstFlag) {
                    ResponseCookie monthCookie = buildCookie(COOKIE_PREFIX_MONTH + shortLink, uvValue,
                            "/" + shortLink, calculateSecondsToNextMonth(now), protocol);
                    response.addCookie(monthCookie);
                }
            } catch (Exception ex) {
                log.error("短链接 [{}] UV Cookie 设置异常", shortLink, ex);
            }

            UvStatsDTO uvStats = UvStatsDTO.builder()
                    // UV 标志位
                    .uvFirstFlag(uvFirstFlag)
                    .uvDayFirstFlag(uvDayFirstFlag)
                    .uvMonthFirstFlag(uvMonthFirstFlag)
                    .shortLink(shortLink)
                    .uvValue(uvValue)
                    // 请求信息
                    .protocol(protocol)
                    .domain(domain)
                    .origin(origin)
                    // 客户端信息（User-Agent解析）
                    .userAgent(userAgentString)
                    .browser(clientInfo.getBrowser())
                    .browserVersion(clientInfo.getBrowserVersion())
                    .os(clientInfo.getOs())
                    .osVersion(clientInfo.getOsVersion())
                    .deviceType(clientInfo.getDeviceType())
                    .deviceBrand(clientInfo.getDeviceBrand())
                    .engine(clientInfo.getEngine())
                    .mobile(clientInfo.getMobile())
                    // 网络信息
                    .clientIp(clientIp)
                    .build();

            // 将 UV 统计信息添加到请求头
            String uvStatsJson = JSON.toJSONString(uvStats);
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(UV_STATS_HEADER, uvStatsJson)
                    .header("X-Short-Link", shortLink)
                    .header("X-Request-Protocol", protocol)
                    .header("X-Request-Domain", domain)
                    .header("X-Client-IP", clientIp)
                    .build();

            log.info("短链接 [{}] UV统计 - 小时:{} 天:{} 月:{} | 设备:{} {} | 浏览器:{} {} | OS:{} | IP:{}",
                    shortLink, uvFirstFlag, uvDayFirstFlag, uvMonthFirstFlag,
                    clientInfo.getDeviceBrand(), clientInfo.getDeviceType(),
                    clientInfo.getBrowser(), clientInfo.getBrowserVersion(),
                    clientInfo.getOs(), clientIp);

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    /**
     * 解析 User-Agent 获取客户端信息
     */
    private UserAgentParseResult parseUserAgent(String userAgentString) {
        UserAgentParseResult result = new UserAgentParseResult();

        if (StrUtil.isBlank(userAgentString)) {
            result.setBrowser("Unknown");
            result.setOs("Unknown");
            result.setEngine("Unknown");
            result.setDeviceType("Unknown");
            result.setDeviceBrand("Unknown");
            result.setMobile(false);
            return result;
        }

        try {
            UserAgent ua = UserAgentUtil.parse(userAgentString);

            result.setBrowser(ua.getBrowser().getName());
            result.setBrowserVersion(ua.getVersion());
            result.setOs(ua.getOs().getName());
            result.setEngine(ua.getEngine().getName());
            result.setMobile(ua.isMobile());

            // 设备类型判断
            if (ua.isMobile()) {
                result.setDeviceType("Mobile");
            } else if (userAgentString.toLowerCase().contains("tablet") ||
                    userAgentString.toLowerCase().contains("ipad")) {
                result.setDeviceType("Tablet");
            } else {
                result.setDeviceType("Desktop");
            }

            // 设备品牌解析
            result.setDeviceBrand(parseDeviceBrand(userAgentString));
            result.setOsVersion(parseOsVersion(userAgentString));

        } catch (Exception e) {
            log.warn("User-Agent 解析失败: {}", userAgentString, e);
            result.setBrowser("Unknown");
            result.setOs("Unknown");
        }

        return result;
    }

    /**
     * 解析设备品牌
     */
    private String parseDeviceBrand(String userAgent) {
        if (StrUtil.isBlank(userAgent)) return "Unknown";

        String ua = userAgent.toLowerCase();

        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("macintosh") || ua.contains("apple")) {
            return "Apple";
        }
        if (ua.contains("samsung")) return "Samsung";
        if (ua.contains("huawei") || ua.contains("honor")) return "Huawei";
        if (ua.contains("xiaomi") || ua.contains("miui") || ua.contains("mi ")) return "Xiaomi";
        if (ua.contains("oppo")) return "OPPO";
        if (ua.contains("vivo")) return "Vivo";
        if (ua.contains("oneplus")) return "OnePlus";
        if (ua.contains("nokia")) return "Nokia";
        if (ua.contains("sony")) return "Sony";
        if (ua.contains("lg")) return "LG";
        if (ua.contains("htc")) return "HTC";
        if (ua.contains("google") || ua.contains("pixel")) return "Google";

        return "Unknown";
    }

    /**
     * 解析操作系统版本
     */
    private String parseOsVersion(String userAgent) {
        if (StrUtil.isBlank(userAgent)) return null;

        String ua = userAgent.toLowerCase();

        // Windows
        if (ua.contains("windows nt 10.0")) return "10";
        if (ua.contains("windows nt 6.3")) return "8.1";
        if (ua.contains("windows nt 6.2")) return "8";
        if (ua.contains("windows nt 6.1")) return "7";
        if (ua.contains("windows nt 6.0")) return "Vista";
        if (ua.contains("windows nt 5.2")) return "XP/Server 2003";
        if (ua.contains("windows nt 5.1")) return "XP";

        // macOS
        if (ua.contains("mac os x")) {
            int idx = ua.indexOf("mac os x ");
            if (idx >= 0) {
                String ver = ua.substring(idx + 9);
                int end = ver.indexOf(';');
                if (end < 0) end = ver.indexOf(')');
                if (end > 0) {
                    return ver.substring(0, end).replace("_", ".");
                }
            }
        }

        // Android
        if (ua.contains("android")) {
            int idx = ua.indexOf("android ");
            if (idx >= 0) {
                String ver = ua.substring(idx + 8);
                int end = ver.indexOf(';');
                if (end > 0) return ver.substring(0, end);
            }
        }

        // iOS
        if (ua.contains("iphone") || ua.contains("ipad")) {
            int idx = ua.indexOf("os ");
            if (idx >= 0) {
                String ver = ua.substring(idx + 3);
                int end = ver.indexOf(' ');
                if (end > 0) {
                    return ver.substring(0, end).replace("_", ".");
                }
            }
        }

        // Linux
        if (ua.contains("ubuntu")) return "Ubuntu";
        if (ua.contains("debian")) return "Debian";
        if (ua.contains("fedora")) return "Fedora";
        if (ua.contains("centos")) return "CentOS";

        return null;
    }

    /**
     * 获取请求协议
     */
    private String getProtocol(ServerHttpRequest request) {
        String protocol = getHeader(request, HEADER_X_FORWARDED_PROTO);
        if (StrUtil.isBlank(protocol)) {
            protocol = getHeader(request, HEADER_X_SCHEME);
        }
        if (StrUtil.isBlank(protocol)) {
            protocol = request.getURI().getScheme();
        }
        return StrUtil.isNotBlank(protocol) ? protocol.toLowerCase() : "http";
    }

    /**
     * 获取域名
     */
    private String getDomain(ServerHttpRequest request) {
        // 1. 优先从 Host 头获取（可能包含端口号，如 shortlink.org:8000）
        String host = getHeader(request, HEADER_HOST);

        // 2. 如果 Host 头不存在，使用请求 URI 的 host
        if (StrUtil.isBlank(host)) {
            host = request.getURI().getHost();
        }

        // 3. 去除端口号（如果存在）
        if (StrUtil.isNotBlank(host) && host.contains(":")) {
            host = host.substring(0, host.lastIndexOf(":"));
        }

        // 4. 返回纯净域名，不做默认值处理
        return host;
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = getHeader(request, HEADER_X_FORWARDED_FOR);
        if (StrUtil.isBlank(ip)) {
            ip = getHeader(request, HEADER_X_REAL_IP);
        }
        if (StrUtil.isBlank(ip)) {
            ip = getHeader(request, "Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip)) {
            ip = getHeader(request, "WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip)) {
            ip = request.getRemoteAddress() != null ?
                    request.getRemoteAddress().getAddress().getHostAddress() : null;
        }

        // 多个 IP 取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 获取请求头
     */
    private String getHeader(ServerHttpRequest request, String headerName) {
        List<String> values = request.getHeaders().get(headerName);
        if (!CollectionUtils.isEmpty(values)) {
            return values.get(0);
        }
        return null;
    }

    /**
     * 构建 Cookie
     */
    private ResponseCookie buildCookie(String name, String value, String path,
                                       long maxAgeSeconds, String protocol) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .path(path)
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .sameSite("Lax");

        if ("https".equalsIgnoreCase(protocol)) {
            builder.secure(true);
        }

        return builder.build();
    }

    /**
     * 计算时间差（秒）
     */
    private long calculateSecondsToNextHour(LocalDateTime now) {
        LocalDateTime next = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        return ChronoUnit.SECONDS.between(now, next);
    }

    private long calculateSecondsToNextDay(LocalDateTime now) {
        LocalDateTime next = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return ChronoUnit.SECONDS.between(now, next);
    }

    private long calculateSecondsToNextMonth(LocalDateTime now) {
        LocalDateTime next = now.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return ChronoUnit.SECONDS.between(now, next);
    }


    /**
     * User-Agent 解析结果
     */
    @Data
    private static class UserAgentParseResult {
        private String browser;
        private String browserVersion;
        private String os;
        private String osVersion;
        private String deviceType;
        private String deviceBrand;
        private String engine;
        private Boolean mobile;
    }

    @Data
    public static class Config {
        private boolean enabled = true;
    }
}