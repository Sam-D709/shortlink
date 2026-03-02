package org.samd.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.samd.shortlink.project.common.conversion.result.Result;
import org.samd.shortlink.project.common.conversion.result.Results;
import org.samd.shortlink.project.dto.resp.LinkDayStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkDefaultStateRespDTO;
import org.samd.shortlink.project.dto.resp.LinkMonthStateRespDTO;
import org.samd.shortlink.project.service.LinkStateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LinkStateController {

    private final LinkStateService linkStateService;

    /**
     * 获取短链接的默认监控数据,包括：过去24h访问量,访客数,设备信息等
      * @param fullshorturl 完整短链接
      * @return 默认监控数据
     */
    @GetMapping("/api/shortlink/project/linkstate/defaultstate")
    public Result<LinkDefaultStateRespDTO> getDefaultLinkState(@RequestParam String fullshorturl) {
        return Results.success(linkStateService.getDefaultLinkState(fullshorturl));
    }

    /**
     * 获取短链接的日监控数据,包括：每天访问量,访客数等
     * @param fullshorturl 完整短链接
     * @param startDate 起始日期，格式为yyyy-MM-dd
     * @param endDate 结束日期，格式为yyyy-MM-dd
     * @return 日监控数据列表
     */
    @GetMapping("/api/shortlink/project/linkstate/daystate")
    public Result<LinkDayStateRespDTO> getDayLinkState(
            @RequestParam String fullshorturl,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Results.success(linkStateService.getDayLinkState(fullshorturl, startDate, endDate));
    }

    /**
     * 获取短链接的月监控数据,包括：每月访问量,访客数等
     * @param fullshorturl 完整短链接
     * @param startMonth 起始月份，格式为yyyy-MM
     * @param endMonth 结束月份，格式为yyyy-MM
     * @return 月监控数据列表
     */
    @GetMapping("/api/shortlink/project/linkstate/monthstate")
    public Result<LinkMonthStateRespDTO> getMonthLinkState(
            @RequestParam String fullshorturl,
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        return Results.success(linkStateService.getMonthLinkState(fullshorturl, startMonth, endMonth));
    }
}
