package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.dto.*;
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
    public ResponseEntity performanceTypeList(@CurrentUser MobileUserDetailsImpl userDetails, PerformanceTypeDto performanceTypeDto) {
        RespBody body = performanceService.performanceTypes(userDetails, performanceTypeDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 获取我提交的所有履职列表
     * @param userDetails
     * @return
     */
    @GetMapping("/performancePage")
    public ResponseEntity performancePage(@CurrentUser MobileUserDetailsImpl userDetails, PerformancePageDto performancePageDto) {
        RespBody body = performanceService.performancePage(userDetails, performancePageDto);
        return ResponseEntity.ok(body);
    }


    /**
     * 添加或修改履职类型列表
     * @param userDetails
     * @return
     */
    @PostMapping("/addOrUpdatePerformance")
    public ResponseEntity addOrUpdatePerformance(@CurrentUser MobileUserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto) {
        RespBody body = performanceService.addOrUpdatePerformance(userDetails,addPerformanceDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 撤回履职信息
     * @return
     */
    @PostMapping("/revokePerformance")
    public ResponseEntity revokePerformance(UidDto uidDto) {
        RespBody body = performanceService.revokePerformance(uidDto.getUid());
        return ResponseEntity.ok(body);
    }

    /**
     * 删除履职信息
     * @return
     */
    @PostMapping("/deletePerformance")
    public ResponseEntity deletePerformance(UidDto uidDto) {
        RespBody body = performanceService.deletePerformance(uidDto.getUid());
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
    /**
     * 查询履职详情
     * @return
     */
    @GetMapping("/performanceDetail")
    public ResponseEntity performanceDetail(ViewDto viewDto) {
        RespBody body = performanceService.performanceDetail(viewDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 该我审核的履职列表
     * @return
     */
    @GetMapping("/performanceAuditorPage")
    public ResponseEntity performanceAuditorPage(@CurrentUser MobileUserDetailsImpl userDetails, PerformancePageDto performancePageDto) {
        RespBody body = performanceService.performanceAuditorPage(userDetails, performancePageDto);
        return ResponseEntity.ok(body);
    }

    /**
     * 审核履职
     * @return
     */
    @GetMapping("/auditPerformance")
    public ResponseEntity auditPerformance(@CurrentUser MobileUserDetailsImpl userDetails, AuditPerformanceDto auditPerformanceDto) {
        RespBody body = performanceService.auditPerformance(userDetails, auditPerformanceDto);
        return ResponseEntity.ok(body);
    }

}
