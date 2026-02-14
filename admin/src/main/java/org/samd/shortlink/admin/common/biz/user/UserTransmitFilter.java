package org.samd.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.samd.shortlink.admin.common.conversion.exception.ClientException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static org.samd.shortlink.admin.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    // 白名单路径：这些请求将完全跳过 token 和 username 检测
    private static final Set<String> WHITE_LIST_PATHS = Set.of("/api/shortlink/admin/user/login",
            "/api/shortlink/admin/user/register",
            "/api/shortlink/admin/user/hasUsername");

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        String requestUri = httpServletRequest.getRequestURI();

        // 判断是否为白名单路径，白名单路径直接放行，不做任何检测
        boolean isWhiteList = WHITE_LIST_PATHS.contains(requestUri);

        if (isWhiteList) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 非白名单路径：必须同时提供 username 和 token
        String username = httpServletRequest.getHeader("username");
        String token = httpServletRequest.getHeader("token");

        if (StrUtil.isBlank(username) || StrUtil.isBlank(token)) {
            throw new ClientException("用户未登录或登录已过期");
        }

        username = username.trim();
        token = token.trim();

        String key = USER_LOGIN_KEY + username;
        // 如果 redis 中不存在该 key 或者没有对应 field，则视为未登录或过期
        boolean hasKey = stringRedisTemplate.hasKey(key);
        if (!hasKey) {
            throw new ClientException("用户未登录或登录已过期");
        }

        Object cached = stringRedisTemplate.opsForHash().get(key, token);
        if (cached == null) {
            throw new ClientException("用户未登录或登录已过期");
        }

        // 解析为 JSON 对象（写入时使用 JSONUtil.toJsonStr(userDO)）
        JSONObject obj;
        try {
            obj = JSONUtil.parseObj(String.valueOf(cached));
        } catch (Exception e) {
            throw new ClientException("用户信息解析失败，可能缓存格式不正确");
        }

        String userId = null;
        // userDO 中 id 字段可能为数字，优先取 id，其次尝试 userId
        if (obj.containsKey("id")) {
            Object idVal = obj.get("id");
            if (idVal != null) userId = String.valueOf(idVal);
        }
        if (StrUtil.isBlank(userId) && obj.containsKey("userId")) {
            Object idVal = obj.get("userId");
            if (idVal != null) userId = String.valueOf(idVal);
        }

        String realName = null;
        if (obj.containsKey("realname")) {
            Object rn = obj.get("realname");
            if (rn != null) realName = String.valueOf(rn);
        }
        if (StrUtil.isBlank(realName) && obj.containsKey("realName")) {
            Object rn = obj.get("realName");
            if (rn != null) realName = String.valueOf(rn);
        }

        UserInfoDTO userInfoDTO = new UserInfoDTO(userId, username, realName);
        UserContext.setUser(userInfoDTO);

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}