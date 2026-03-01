package org.samd.shortlink.project.mq.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 消息队列幂等处理器（三状态：未处理/处理中/已完成）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageQueueIdempotentHandler {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String IDEMPOTENT_KEY_PREFIX = "shortlink:mq:idempotent:";

    /**
     * 判断消息是否首次消费（原子性设置"处理中"状态）
     * @param messageId 消息唯一标识
     * @return true=首次消费，false=重复消息或正在处理
     */
    public boolean isMessageProcessed(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        // 设置"0"表示处理中，2分钟过期（防止死锁）
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue()
                        .setIfAbsent(key, "0", 2, TimeUnit.MINUTES)
        );
    }

    /**
     * 判断消息是否已完全处理成功
     */
    public boolean isAccomplish(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        return Objects.equals(stringRedisTemplate.opsForValue().get(key), "1");
    }

    /**
     * 标记消息处理完成
     */
    public void setAccomplish(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        stringRedisTemplate.opsForValue().set(key, "1", 2, TimeUnit.MINUTES);
        log.debug("[Redis]消息处理完成标记: {}", messageId);
    }

    /**
     * 删除幂等标识
     */
    public void deleteProcessed(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        stringRedisTemplate.delete(key);
        log.warn("[Redis]删除幂等标记: {}", messageId);
    }
}