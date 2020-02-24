package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.OpinionPageDto;
import com.cdkhd.npc.entity.dto.StatisticalPageDto;
import com.cdkhd.npc.vo.RespBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface StatisticalService {

    /**
     * 代表履职统计
     * @param userDetails
     * @param statisticalPageDto
     * @return
     */
    RespBody memberPerformance(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto);

    /**
     * 各镇履职统计
     * @param statisticalPageDto
     * @return
     */
    RespBody townPerformance(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto);

    void exportStatistical(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto, HttpServletRequest req, HttpServletResponse res);

    void exportTownStatistical(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto, HttpServletRequest req, HttpServletResponse res);
}
