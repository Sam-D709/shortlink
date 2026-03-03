package org.samd.shortlink.project.common.constant;

/**
 * 短链接后管 Redis 缓存常量类
 */
public class RedisCacheConstant {

    /**
     * 路由表标识
     */
    public static final String GOTO_FULL_SHORT_LINK_KEY = "shortlink:shortlink_goto_%s:";

    /**
     * 路由表分布式锁标识
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "shortlink:lock_shortlink_goto_%s:";

    /**
     * 路由表空值标识
     */
    public static final String GOTO_FULL_SHORT_LINK_NULL_KEY = "shortlink:shortlink_goto_null_%s:";

    /**
     * 延时双删默认延迟（毫秒）
     */
    public static final long DELAY_DELETE_SHORT_LINK_CACHE_MILLIS = 500L;
}