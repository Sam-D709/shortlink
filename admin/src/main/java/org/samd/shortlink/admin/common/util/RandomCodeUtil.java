package org.samd.shortlink.admin.common.util;

import java.security.SecureRandom;

/**
 * 随机码工具类（静态工具类）
 * 生成由数字和大小写字母组成的随机字符串，默认长度为6位。
 */
public final class RandomCodeUtil {

    private static final String CHAR_POOL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 6;

    private RandomCodeUtil() {
        // 工具类不允许实例化
    }

    /**
     * 生成默认长度的随机码（6位）
     * @return 随机码字符串
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的随机码
     * @param length 期望的长度，必须为正数
     * @return 随机码字符串
     * @throws IllegalArgumentException 如果 length <= 0
     */
    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = RANDOM.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(idx));
        }
        return sb.toString();
    }
}
