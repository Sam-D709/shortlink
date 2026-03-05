package org.samd.shortlink.project.mq.main;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
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
            // 异步发送，2秒超时
            rocketMQTemplate.asyncSend(topic, build, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    if (sendResult != null && sendResult.getSendStatus() != null
                            && "SEND_OK".equals(sendResult.getSendStatus().name())) {
                        log.info("[生产者]发送成功: msgId={}, keys={}", sendResult.getMsgId(), keys);
                        return;
                    }

                    String error = sendResult == null
                            ? "sendResult is null"
                            : "发送状态异常: " + sendResult.getSendStatus();
                    log.error("[生产者]异步发送状态异常，转入延迟队列: keys={}, error={}", keys, error);
                    delayRetryProducer.sendToRetry(keys, payloadJson, "STATS_MESSAGE", error);
                }

                @Override
                public void onException(Throwable ex) {
                    String error = ex.getMessage();
                    log.error("[生产者]异步发送失败，转入延迟队列: keys={}, error={}", keys, error);
                    delayRetryProducer.sendToRetry(keys, payloadJson, "STATS_MESSAGE", error);
                }
            }, 2000L);

        } catch (Throwable ex) {
            String error = ex.getMessage();
            log.error("[生产者]发起异步发送失败，转入延迟队列: keys={}, error={}", keys, error);

            delayRetryProducer.sendToRetry(
                    keys,
                    payloadJson,
                    "STATS_MESSAGE",
                    error
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