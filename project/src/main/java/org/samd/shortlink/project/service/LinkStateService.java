package org.samd.shortlink.project.service;

import org.samd.shortlink.project.dto.resp.LinkDayStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkDefaultStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkMonthStateRespDTO;

/**
 * 短链接获取监控数据
 */
public interface LinkStateService{

    LinkDefaultStateRespDTO getDefaultLinkState(String fullshorturl);

    LinkDayStateRespDTO getDayLinkState(String fullshorturl, String startDate, String endDate);

    LinkMonthStateRespDTO getMonthLinkState(String fullshorturl, String startMonth, String endMonth);
}
