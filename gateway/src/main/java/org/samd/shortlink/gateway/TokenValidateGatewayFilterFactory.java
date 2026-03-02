package org.samd.shortlink.gateway;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Token验证网关过滤器工厂
 * 用于验证请求头中的username和token是否有效
 */
@Slf4j
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<TokenValidateGatewayFilterFactory.Config> {

    private final StringRedisTemplate stringRedisTemplate;

    // Redis中存储用户登录信息的Key前缀，与登录逻辑保持一致
    private static final String USER_LOGIN_KEY = "shortlink:login:";

    public TokenValidateGatewayFilterFactory(StringRedisTemplate stringRedisTemplate) {
        super(Config.class);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getURI().getPath();

            List<String> whitePathList = config.getWhitePathList();
            if (!CollectionUtils.isEmpty(whitePathList)) {
                for (String whitePath : whitePathList) {
                    // 使用AntPathMatcher风格的路径匹配，支持通配符
                    if (antPathMatch(whitePath, requestPath)) {
                        log.info("请求路径 [{}] 在白名单中，无需验证Token", requestPath);
                        return chain.filter(exchange);
                    }
                }
            }

            HttpHeaders headers = request.getHeaders();
            String username = headers.getFirst("username");
            String token = headers.getFirst("token");

            if (!StringUtils.hasText(username) || !StringUtils.hasText(token)) {
                log.warn("请求缺少必要的认证参数，路径: [{}], username: [{}], token: [{}]",
                        requestPath, username, token);
                return unauthorizedResponse(exchange.getResponse(), "缺少认证信息，请重新登录");
            }

            String loginKey = USER_LOGIN_KEY + username;
            try {
                Boolean hasKey = stringRedisTemplate.hasKey(loginKey);
                if (!hasKey) {
                    log.warn("用户 [{}] 的登录信息已过期或不存在", username);
                    return unauthorizedResponse(exchange.getResponse(), "登录已过期，请重新登录");
                }
                Object userInfo = stringRedisTemplate.opsForHash().get(loginKey, token);
                if (userInfo == null) {
                    log.warn("用户 [{}] 的token [{}] 无效", username, token);
                    return unauthorizedResponse(exchange.getResponse(), "Token无效，请重新登录");
                }

                stringRedisTemplate.expire(loginKey, 30L, TimeUnit.DAYS);
                log.info("用户 [{}] Token验证通过，刷新会话过期时间", username);

                UserInfoDTO userInfoDTO = JSONUtil.toBean(userInfo.toString(), UserInfoDTO.class);
                String userInfoJson = JSONUtil.toJsonStr(userInfoDTO);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Info", userInfoJson)
                        .header("X-User-Name", username)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                log.error("Token验证过程中发生异常，用户: [{}]", username, e);
                return unauthorizedResponse(exchange.getResponse(), "验证服务异常，请稍后重试");
            }
        };
    }

    private boolean antPathMatch(String pattern, String path) {
        String regex = pattern
                .replace("**", "{{DOUBLE_STAR}}")
                .replace("*", "[^/]*")
                .replace("{{DOUBLE_STAR}}", ".*")
                .replace("?", ".");

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!pattern.startsWith("/")) {
            pattern = "/" + pattern;
        }

        return path.matches(regex);
    }

    /**
     * 返回401未授权的响应
     */
    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"code\": 401, \"message\": \"%s\", \"data\": null}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 配置类，用于接收配置文件中的参数
     */
    @Data
    public static class Config {
        private List<String> whitePathList;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return java.util.Collections.singletonList("whitePathList");
    }
}