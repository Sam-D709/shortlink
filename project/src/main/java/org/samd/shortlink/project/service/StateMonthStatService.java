package org.samd.shortlink.project.service;

public interface StateMonthStatService {
    /**
     * 汇总上月每个短链接每天的设备、浏览器、操作系统状态，写入对应月表
     */
    void statAndSaveLastMonth();
}