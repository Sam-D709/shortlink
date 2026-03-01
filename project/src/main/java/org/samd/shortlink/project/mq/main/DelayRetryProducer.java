package org.samd.shortlink.project.mq.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.samd.shortlink.project.mq.entity.DelayRetryMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MQ 失败降级延迟队列生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayRetryProducer {

    private final RedissonClient redissonClient;

    @Value("${mq.delay.retry-key:shortlink:delay:retry}")
    private String delayQueueKey;

    @Value("${mq.delay.max-retry:3}")
    private int maxRetryCount;

    @Value("${mq.delay.initial-delay:5}")
    private long initialDelaySeconds;

    /**
     * 发送失败时，放入延迟队列重试
     */
    public void sendToRetry(String originalMessageId, String payloadJson,
                            String messageType, String failReason) {
        DelayRetryMessage retryMsg = DelayRetryMessage.builder()
                .originalMessageId(originalMessageId)
                .retryId(UUID.randomUUID().toString())
                .retryCount(0)
                .maxRetryCount(maxRetryCount)
                .delaySeconds(initialDelaySeconds)
                .payloadJson(payloadJson)
                .messageType(messageType)
                .firstCreateTime(LocalDateTime.now())
                .currentRetryTime(LocalDateTime.now())
                .lastFailReason(failReason)
                .build();

        RBlockingDeque<DelayRetryMessage> blockingDeque =
                redissonClient.getBlockingDeque(delayQueueKey);
        RDelayedQueue<DelayRetryMessage> delayedQueue =
                redissonClient.getDelayedQueue(blockingDeque);

        // 延迟 initialDelaySeconds 后放入队列
        delayedQueue.offer(retryMsg, initialDelaySeconds, TimeUnit.SECONDS);

        log.warn("[延迟队列] 消息进入重试队列: originalId={}, retryId={}, delay={}s, reason={}",
                originalMessageId, retryMsg.getRetryId(), initialDelaySeconds, failReason);
    }

    /**
     * 重试再次失败，继续延迟（指数退避）
     */
    public void requeueWithBackoff(DelayRetryMessage oldMsg, String newFailReason) {
        if (oldMsg.getRetryCount() >= oldMsg.getMaxRetryCount()) {
            log.error("[延迟队列] 超过最大重试次数，放弃: originalId={}",
                    oldMsg.getOriginalMessageId());
            // 可转入死信队列或本地文件
            saveToDeadLetter(oldMsg, newFailReason);
            return;
        }

        // 指数退避：5, 10, 20, 40...
        long newDelay = oldMsg.getDelaySeconds() * 2;

        DelayRetryMessage newMsg = DelayRetryMessage.builder()
                .originalMessageId(oldMsg.getOriginalMessageId())
                .retryId(UUID.randomUUID().toString())
                .retryCount(oldMsg.getRetryCount() + 1)
                .maxRetryCount(oldMsg.getMaxRetryCount())
                .delaySeconds(newDelay)
                .payloadJson(oldMsg.getPayloadJson())
                .messageType(oldMsg.getMessageType())
                .firstCreateTime(oldMsg.getFirstCreateTime())
                .currentRetryTime(LocalDateTime.now())
                .lastFailReason(newFailReason)
                .build();

        RBlockingDeque<DelayRetryMessage> blockingDeque =
                redissonClient.getBlockingDeque(delayQueueKey);
        RDelayedQueue<DelayRetryMessage> delayedQueue =
                redissonClient.getDelayedQueue(blockingDeque);

        delayedQueue.offer(newMsg, newDelay, TimeUnit.SECONDS);

        log.warn("[延迟队列] 第{}次重试: originalId={}, delay={}s",
                newMsg.getRetryCount(), oldMsg.getOriginalMessageId(), newDelay);
    }

    private void saveToDeadLetter(DelayRetryMessage msg, String reason) {
        // 转入死信队列或本地文件（简化版直接记录日志）
        log.error("[死信] 消息最终失败: originalId={}, payload={}, reason={}",
                msg.getOriginalMessageId(), msg.getPayloadJson(), reason);
        // 实际可写入数据库或本地文件
    }
}