package org.samd.shortlink.project.mq.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.samd.shortlink.project.dao.entity.*;
import org.samd.shortlink.project.mq.entity.StatsMessage;
import org.samd.shortlink.project.mq.util.MessageQueueIdempotentHandler;
import org.samd.shortlink.project.mq.util.StatsAggregator;
import org.springframework.stereotype.Component;

/**
 * 短链接统计消息消费者（参考示例的三状态幂等设计）
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "${mq.stats.topic:SHORT_LINK_STATS}",
        consumerGroup = "${mq.consumer.group:stats-consumer-group}")
public class StatsConsumer implements RocketMQListener<StatsMessage> {

    private final StatsAggregator aggregator;
    private final MessageQueueIdempotentHandler idempotentHandler;

    @Override
    public void onMessage(StatsMessage msg) {
        String messageId = msg.getMessageId();

        if (!idempotentHandler.isMessageProcessed(messageId)) {
            // 只要插入失败代表正在处理或者已经完成，都需要判断状态
            if (idempotentHandler.isAccomplish(messageId)) {
                log.info("[消费者]消息已完成，跳过重复消费: {}", messageId);
                return;
            }
            // 正在处理中，抛异常让MQ重试
            log.warn("[消费者]消息处理中，触发重试: {}", messageId);
            throw new RuntimeException("[消费者]消息正在处理中，需要重试");
        }

        try {
            log.debug("[消费者]首次消费消息: {}", messageId);
            processMessage(msg);

        } catch (Exception ex) {
            log.error("[消费者]消息处理异常: {}", messageId, ex);
            try {
                idempotentHandler.deleteProcessed(messageId);
            } catch (Throwable delEx) {
                log.error("[消费者]清理幂等标记失败: {}", messageId, delEx);
                //如果清理失败那么这个数据将会随着三次尝试之后被抛出给死信机制
            }
            throw ex;
        }

        idempotentHandler.setAccomplish(messageId);
        log.debug("[消费者]消息处理完成: {}", messageId);
    }

    //下面是具体的统计处理逻辑
    private void processMessage(StatsMessage msg) {
        processHour(msg);
        processDay(msg);
        processMonth(msg);
        processOS(msg);
        processBrowser(msg);
        processDevice(msg);
    }

    private void processHour(StatsMessage msg) {
        AccessStateHourDO hour = new AccessStateHourDO();
        hour.setFullshorturl(msg.getFullShortUrl());
        hour.setDate(msg.getDate());
        hour.setHour(msg.getHour());
        hour.setPv(1);
        hour.setUv(msg.getUvFirstFlag() != null && msg.getUvFirstFlag() ? 1 : 0);
        aggregator.aggregateHour(hour);
    }

    private void processDay(StatsMessage msg) {
        AccessStateDayDO day = new AccessStateDayDO();
        day.setFullshorturl(msg.getFullShortUrl());
        day.setDate(msg.getDate());
        day.setPv(1);
        day.setUv(msg.getUvDayFirstFlag() != null && msg.getUvDayFirstFlag() ? 1 : 0);
        aggregator.aggregateDay(day);
    }

    private void processMonth(StatsMessage msg) {
        AccessStateMonthDO month = new AccessStateMonthDO();
        month.setFullshorturl(msg.getFullShortUrl());
        month.setYear(String.valueOf(msg.getYear()));
        month.setMonth(msg.getMonth());
        month.setPv(1);
        month.setUv(msg.getUvMonthFirstFlag() != null && msg.getUvMonthFirstFlag() ? 1 : 0);
        aggregator.aggregateMonth(month);
    }

    private void processOS(StatsMessage msg) {
        if (msg.getOs() == null) return;
        OSStateDO os = new OSStateDO();
        os.setFullshorturl(msg.getFullShortUrl());
        os.setDate(msg.getDate());
        os.setOs(msg.getOs());
        os.setCnt(1);
        aggregator.aggregateOS(os);
    }

    private void processBrowser(StatsMessage msg) {
        if (msg.getBrowser() == null) return;
        BrowserStateDO browser = new BrowserStateDO();
        browser.setFullshorturl(msg.getFullShortUrl());
        browser.setDate(msg.getDate());
        browser.setBrowser(msg.getBrowser());
        browser.setCnt(1);
        aggregator.aggregateBrowser(browser);
    }

    private void processDevice(StatsMessage msg) {
        if (msg.getDevice() == null) return;
        DeviceStateDO device = new DeviceStateDO();
        device.setFullshorturl(msg.getFullShortUrl());
        device.setDate(msg.getDate());
        device.setDevice(msg.getDevice());
        device.setCnt(1);
        aggregator.aggregateDevice(device);
    }
}