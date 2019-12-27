package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AddPerformanceDto;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/performance")
public class PerformanceApi {

    private PerformanceService performanceService;

    @Autowired
    public PerformanceApi(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    /**
     * 获取履职类型列表
     * @param userDetails
     * @return
     */
    @GetMapping("/performanceTypeList")
    public ResponseEntity performanceTypeList(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = performanceService.performanceTypeList(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取履职类型列表
     * @param userDetails
     * @return
     */
    @PostMapping("/addOrUpdatePerformance")
    public ResponseEntity addOrUpdatePerformance(@CurrentUser UserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto) {
        RespBody body = performanceService.addOrUpdatePerformance(userDetails,addPerformanceDto);
        return ResponseEntity.ok(body);
    }

}
