package org.samd.shortlink.project.mq.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * 数据库插入消息体
 * 支持任意表的插入操作
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    // 消息ID
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    // 短链接（业务Key）
    @NotBlank
    private String fullShortUrl;

    // 时间维度数据
    private Integer hour;           // HOUR类型用
    private Integer year;           // MONTH类型用
    private Integer month;          // MONTH类型用
    private Date date;              // 统一日期

    // 统计值
    private Integer pv = 0;
    private Integer uv = 0;

    // 设备信息
    private String os;
    private String browser;
    private String device;

    // 标记位（用于计算UV）
    private Boolean uvFirstFlag;
    private Boolean uvDayFirstFlag;
    private Boolean uvMonthFirstFlag;

    // 创建时间
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();
}