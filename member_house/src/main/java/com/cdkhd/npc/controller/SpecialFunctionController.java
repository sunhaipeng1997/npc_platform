package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.ListUidDto;
import com.cdkhd.npc.service.SpecialFunctionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house/special_function")
public class SpecialFunctionController {

    private SpecialFunctionService specialFunctionService;

    @Autowired
    public SpecialFunctionController(SpecialFunctionService specialFunctionService) {
        this.specialFunctionService = specialFunctionService;
    }

    /**
     * 获取设置好的
     * @return
     */
    @GetMapping("/getSettings")
    public ResponseEntity getTodayNumber(@CurrentUser UserDetailsImpl userDetails) {
        RespBody body = specialFunctionService.getSettings(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 新闻审核人
     * @return
     */
    @PostMapping("/newsAuditor")
    public ResponseEntity newsAuditor(@CurrentUser UserDetailsImpl userDetails, ListUidDto baseDto) {
        RespBody body = specialFunctionService.newsAuditor(userDetails,baseDto.getUids());
        return ResponseEntity.ok(body);
    }

    /**
     * 通知审核人
     * @return
     */
    @PostMapping("/notificationAuditor")
    public ResponseEntity notificationAuditor(@CurrentUser UserDetailsImpl userDetails, ListUidDto baseDto) {
        RespBody body = specialFunctionService.notificationAuditor(userDetails,baseDto.getUids());
        return ResponseEntity.ok(body);
    }

    /**
     * 建议接受人
     * @return
     */
    @PostMapping("/adviceReceiver")
    public ResponseEntity adviceReceiver(@CurrentUser UserDetailsImpl userDetails, ListUidDto baseDto) {
        RespBody body = specialFunctionService.adviceReceiver(userDetails,baseDto.getUids());
        return ResponseEntity.ok(body);
    }


    /**
     * 履职总审核
     * @return
     */
    @PostMapping("/performanceAuditorManager")
    public ResponseEntity performanceAuditorManager(@CurrentUser UserDetailsImpl userDetails, ListUidDto baseDto) {
        RespBody body = specialFunctionService.performanceAuditorManager(userDetails,baseDto.getUids());
        return ResponseEntity.ok(body);
    }

    /**
     * 履职小组审核人
     * @return
     */
    @PostMapping("/performanceGroupAuditor")
    public ResponseEntity performanceGroupAuditor(String group, String uid) {
        RespBody body = specialFunctionService.performanceGroupAuditor(group, uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 履职镇审核人
     * @return
     */
    @PostMapping("/performanceTownAuditor")
    public ResponseEntity newsAuditor(String town, String uid) {
        RespBody body = specialFunctionService.performanceTownAuditor(town,uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 开关
     * @return
     */
    @PostMapping("/auditorSwitch")
    public ResponseEntity auditorSwitch(@CurrentUser UserDetailsImpl userDetails, Boolean switches) {
        RespBody body = specialFunctionService.auditorSwitch(userDetails,switches);
        return ResponseEntity.ok(body);
    }

}
