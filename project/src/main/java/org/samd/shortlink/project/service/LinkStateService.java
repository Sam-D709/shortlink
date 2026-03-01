package org.samd.shortlink.project.service;

import org.samd.shortlink.project.dto.req.LinkDayStateReqDTO;
import org.samd.shortlink.project.dto.req.LinkMonthStateReqDTO;
import org.samd.shortlink.project.dto.resp.LinkDayStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkDefaultStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkMonthStateRespDTO;

/**
 * 短链接获取监控数据
 */
public interface LinkStateService{

    LinkDefaultStateRespDTO getDefaultLinkState(String fullshorturl);
    LinkDayStateRespDTO getDayLinkState(LinkDayStateReqDTO requestParam);
    LinkMonthStateRespDTO getMonthLinkState(LinkMonthStateReqDTO requestParam);
}
