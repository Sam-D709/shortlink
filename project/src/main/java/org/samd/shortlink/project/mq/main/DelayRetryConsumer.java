package org.samd.shortlink.project.mq.main;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.samd.shortlink.project.mq.entity.DelayRetryMessage;
import org.samd.shortlink.project.mq.entity.StatsMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

/**
 * 延迟队列消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayRetryConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final StatsProducer statsProducer;
    private final DelayRetryProducer delayRetryProducer;

    @Value("${mq.delay.retry-key:shortlink:delay:retry}")
    private String delayQueueKey;

    @Value("${mq.delay.consumer-enabled:true}")
    private boolean consumerEnabled;

    @Override
    public void afterPropertiesSet() {
        if (!consumerEnabled) {
            log.info("[延迟队列] 消费者已禁用");
            return;
        }
        startConsumer();
    }

    /**
     * 启动独立消费线程
     */
    public void startConsumer() {
        // 使用单线程池，线程为守护线程，命名为 delay-retry-consumer
        Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("delay-retry-consumer");
            thread.setDaemon(true);
            return thread;
        }).execute(() -> {
            log.info("[延迟队列] 消费者线程启动");
            //主要消费队列
            RBlockingDeque<DelayRetryMessage> blockingDeque =
                    redissonClient.getBlockingDeque(delayQueueKey);
            //实现延迟功能队列
            RDelayedQueue<DelayRetryMessage> delayedQueue =
                    redissonClient.getDelayedQueue(blockingDeque);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 阻塞获取（最多等待500ms检查中断）
                    DelayRetryMessage message = delayedQueue.poll();

                    if (message != null) {
                        processRetry(message);
                    } else {
                        // 无消息时短暂休眠，避免CPU空转
                        LockSupport.parkNanos(500_000_000L); // 500ms
                    }
                }catch (Throwable e) {
                    log.error("[延迟队列] 消费异常", e);
                    // 异常不退出，继续循环
                }
            }
        });
    }

    /**
     * 处理重试消息
     */
    private void processRetry(DelayRetryMessage retryMsg) {
        log.info("[延迟队列] 开始重试尝试: originalId={}, 第{}次",
                retryMsg.getOriginalMessageId(), retryMsg.getRetryCount() + 1);

        try {
            // 反序列化原始消息
            StatsMessage originalMsg = JSON.parseObject(
                    retryMsg.getPayloadJson(), StatsMessage.class);

            // 重新发送MQ（同步发送，确保可靠）
            statsProducer.sendWithRetry(originalMsg);

            log.info("[延迟队列] 重试成功: originalId={}", retryMsg.getOriginalMessageId());

        } catch (Exception e) {
            log.error("[延迟队列] 重试失败: originalId={}, error={}",
                    retryMsg.getOriginalMessageId(), e.getMessage());

            // 再次进入延迟队列生产者
            delayRetryProducer.requeueWithBackoff(retryMsg, e.getMessage());
        }
    }
}