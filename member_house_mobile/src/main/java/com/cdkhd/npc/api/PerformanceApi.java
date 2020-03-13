package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.AddPerformanceDto;
import com.cdkhd.npc.entity.dto.PerformancePageDto;
import com.cdkhd.npc.entity.dto.PerformanceTypeDto;
import com.cdkhd.npc.entity.dto.UidDto;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity performanceTypeList(@CurrentUser UserDetailsImpl userDetails, PerformanceTypeDto performanceTypeDto) {
        RespBody body = performanceService.performanceTypes(userDetails, performanceTypeDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取我提交的所有履职列表
     * @param userDetails
     * @return
     */
    @GetMapping("/performancePage")
    public ResponseEntity performancePage(@CurrentUser UserDetailsImpl userDetails, PerformancePageDto performancePageDto) {
        RespBody body = performanceService.performancePage(userDetails, performancePageDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 添加或修改履职类型列表
     * @param userDetails
     * @return
     */
    @PostMapping("/addOrUpdatePerformance")
    public ResponseEntity addOrUpdatePerformance(@CurrentUser UserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto) {
        RespBody body = performanceService.addOrUpdatePerformance(userDetails,addPerformanceDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 删除履职信息
     * @return
     */
    @DeleteMapping("/deletePerformance")
    public ResponseEntity deletePerformance(String uid) {
        RespBody body = performanceService.deletePerformance(uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 根据代表uid分页获取履职信息列表
     * @return
     */
    @GetMapping("/performanceList")
    public ResponseEntity performanceList(UidDto uidDto) {
        RespBody body = performanceService.performanceList(uidDto);
        return ResponseEntity.ok(body);
    }

}
