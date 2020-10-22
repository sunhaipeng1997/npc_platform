package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.StatisticalPageDto;
import com.cdkhd.npc.service.StatisticalService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/member_house/statistic")
public class StatisticalController {

    private StatisticalService statisticalService;

    @Autowired
    public StatisticalController(StatisticalService statisticalService) {
        this.statisticalService = statisticalService;
    }

    /**
     * 代表履职统计
     * @return
     */
    @GetMapping("/memberPerformance")
    public ResponseEntity memberPerformance(@CurrentUser UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto) {
        RespBody body = statisticalService.memberPerformance(userDetails, statisticalPageDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 各镇履职统计
     * @return
     */
    @GetMapping("/townPerformance")
    public ResponseEntity townPerformance(@CurrentUser UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto) {
        RespBody body = statisticalService.townPerformance(userDetails, statisticalPageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 导出代表履职统计
     * @return
     */
    @PostMapping("/exportStatistical")
    public void exportStatistical(@CurrentUser UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto, HttpServletRequest req, HttpServletResponse res) {
        statisticalService.exportStatistical(userDetails,statisticalPageDto,req,res);
    }

    /**
     * 导出各镇履职统计
     * @return
     */
    @PostMapping("/exportTownStatistical")
    public void exportTownStatistical(@CurrentUser UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto, HttpServletRequest req, HttpServletResponse res) {
        statisticalService.exportTownStatistical(userDetails,statisticalPageDto,req,res);
    }

}
