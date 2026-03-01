package org.samd.shortlink.project.mq.main;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.samd.shortlink.project.mq.entity.StatsMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 短链接统计消息生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatsProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final DelayRetryProducer delayRetryProducer;  // 降级队列

    @Value("${mq.stats.topic:SHORT_LINK_STATS}")
    private String topic;

    /**
     * 主发送方法
     */
    public void send(StatsMessage message) {
        String keys = message.getMessageId();
        String payloadJson = JSON.toJSONString(message);

        Message<StatsMessage> build = MessageBuilder
                .withPayload(message)
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .build();

        try {
            // 同步发送，2秒超时
            SendResult sendResult = rocketMQTemplate.syncSend(topic, build, 2000L);

            if (sendResult.getSendStatus().name().equals("SEND_OK")) {
                log.info("[生产者]发送成功: msgId={}, keys={}",
                        sendResult.getMsgId(), keys);
                return;
            }

            // 状态异常，进入降级
            throw new RuntimeException("[生产者]发送状态异常: " + sendResult.getSendStatus());

        } catch (Throwable ex) {
            log.error("[生产者]发送信息失败，转入延迟队列: keys={}, error={}", keys, ex.getMessage());

            delayRetryProducer.sendToRetry(
                    keys,
                    payloadJson,
                    "STATS_MESSAGE",
                    ex.getMessage()
            );
        }
    }

    /**
     * 延迟队列重试时调用
     */
    public void sendWithRetry(StatsMessage message) throws Exception {
        String keys = message.getMessageId();

        Message<StatsMessage> build = MessageBuilder
                .withPayload(message)
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .build();

        // 同步发送，异常直接抛出，由DelayRetryConsumer捕获
        SendResult sendResult = rocketMQTemplate.syncSend(topic, build, 2000L);

        if (!sendResult.getSendStatus().name().equals("SEND_OK")) {
            throw new RuntimeException("[生产者]重试发送失败: " + sendResult.getSendStatus());
        }
    }
}