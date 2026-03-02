package org.samd.shortlink.project.service;

import org.samd.shortlink.project.dto.resp.LinkDayStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkDefaultStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkMonthStateRespDTO;

/**
 * 短链接获取监控数据
 */
public interface LinkStateService{
    /**
     * 获取短链接的默认监控数据,包括：过去24h访问量,访客数,设备信息等
     * @param fullshorturl 完整短链接
     * @return 默认监控数据
     */
    LinkDefaultStateRespDTO getDefaultLinkState(String fullshorturl);

    /**
     * 获取短链接的日监控数据,包括：每天访问量,访客数等
     * @param fullshorturl 完整短链接
     * @param startDate 起始日期，格式为yyyy-MM-dd
     * @param endDate 结束日期，格式为yyyy-MM-dd
     * @return 日监控数据列表
     */
    LinkDayStateRespDTO getDayLinkState(String fullshorturl, String startDate, String endDate);

    /**
     * 获取短链接的月监控数据,包括：每月访问量,访客数等
     * @param fullshorturl 完整短链接
     * @param startMonth 起始月份，格式为yyyy-MM
     * @param endMonth 结束月份，格式为yyyy-MM
     * @return 月监控数据列表
     */
    LinkMonthStateRespDTO getMonthLinkState(String fullshorturl, String startMonth, String endMonth);
}
