package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.ListUidDto;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.service.SpecialFunctionService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/special_function")
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
     * 建议接受人
     * @return
     */
    @PostMapping("/adviceReceiver")
    public ResponseEntity adviceReceiver(@CurrentUser UserDetailsImpl userDetails, ListUidDto baseDto) {
        RespBody body = specialFunctionService.adviceReceiver(userDetails,baseDto.getUids());
        return ResponseEntity.ok(body);
    }



}
