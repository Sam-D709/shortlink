package org.samd.shortlink.admin.constant;

/**
 * 短链接后管 Redis 缓存常量类
 */
public class RedisCacheConstant {

    /**
     * 用户注册分布式锁
     */
    public static final String LOCK_USER_REGISTER_KEY = "shortlink:lock_user-register:";

    public static final String USER_LOGIN_KEY = "shortlink:login:";
}