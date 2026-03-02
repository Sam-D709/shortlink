package org.samd.shortlink.admin.common.util;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import org.samd.shortlink.admin.common.conversion.exception.ClientException;
import org.samd.shortlink.admin.dto.req.UserInfoDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文工具类
 * 用于在任意地方获取当前登录用户信息
 */
@Component
public class UserContext {

    /**
     * 请求头中的用户信息 Key
     */
    private static final String USER_INFO_HEADER = "X-User-Info";

    /**
     * 请求头中的用户名 Key
     */
    private static final String USER_NAME_HEADER = "X-User-Name";

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
     * 获取当前登录用户信息
     *
     * @return UserInfoDTO，如果未登录返回 null
     */
    public static UserInfoDTO getUser() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String userInfoJson = request.getHeader(USER_INFO_HEADER);
        if (userInfoJson == null || userInfoJson.isEmpty()) {
            return null;
        }

        try {
            return JSON.parseObject(userInfoJson, UserInfoDTO.class);
        } catch (Exception e) {
            // 解析失败，可能是格式问题
            return null;
        }
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，如果未登录返回 null
     */
    public static Long getUserId() {
        UserInfoDTO user = getUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，如果未登录返回 null
     */
    public static String getUsername() {
        // 优先从用户信息对象获取
        UserInfoDTO user = getUser();
        if (user != null && user.getUsername() != null) {
            return user.getUsername();
        }

        // 备用：直接从请求头获取
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(USER_NAME_HEADER);
    }

    /**
     * 检查当前是否已登录
     *
     * @return true 已登录，false 未登录
     */
    public static boolean isLogin() {
        return getUser() != null;
    }

    /**
     * 获取当前登录用户的指定信息
     * 如果不存在则抛出异常
     */
    public static UserInfoDTO getUserOrThrow() {
        UserInfoDTO user = getUser();
        if (user == null) {
            throw new ClientException("用户未登录或登录已过期");
        }
        return user;
    }

    /**
     * 获取当前登录用户ID
     * 如果不存在则抛出异常
     */
    public static Long getUserIdOrThrow() {
        Long userId = getUserId();
        if (userId == null) {
            throw new ClientException("用户未登录或登录已过期");
        }
        return userId;
    }
}