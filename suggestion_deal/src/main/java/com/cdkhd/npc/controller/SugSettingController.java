package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.SugSettingDto;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.service.SugSettingService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/suggestion_deal/sug_setting")
public class SugSettingController {

    private SugSettingService sugSettingService;

    @Autowired
    public SugSettingController(SugSettingService sugSettingService) {
        this.sugSettingService = sugSettingService;
    }


    /**
     * 获取
     */
    @GetMapping("/getSetting")
    public ResponseEntity getSetting(@CurrentUser UserDetailsImpl userDetails) {
        String uid = "";
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            uid = userDetails.getArea().getUid();
        }else {
            uid = userDetails.getTown().getUid();
        }
        RespBody body = sugSettingService.getSugSettings(userDetails.getLevel(), uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 保存设置
     */
    @PostMapping("/saveSugSetting")
    public ResponseEntity saveSugSetting(@CurrentUser UserDetailsImpl userDetails, SugSettingDto sugSettingDto) {
        RespBody body = sugSettingService.saveSugSetting(userDetails, sugSettingDto);
        return ResponseEntity.ok(body);
    }
}
