package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.PerformanceTypeDto;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house/performance")
public class PerformanceController {

    private PerformanceService performanceService;

    @Autowired
    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    /**
     * 获取履职类型列表
     * @return
     */
    @GetMapping("/performanceType")
    public ResponseEntity performanceType(@CurrentUser UserDetailsImpl userDetails, PerformanceTypeDto performanceTypeDto) {
        RespBody body = performanceService.findPerformanceType(userDetails,performanceTypeDto);
        return ResponseEntity.ok(body);
    }

}
