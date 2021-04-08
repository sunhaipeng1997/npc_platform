package com.cdkhd.npc.controller;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.entity.dto.SystemSettingDto;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.service.SystemService;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/manager/systemSetting")
public class SystemSettingController {
    private SystemSettingService systemSettingService;

    @Autowired
    public SystemSettingController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }


    /**
     * 获取系统配置
     * @param userDetails
     * @return
     */
    @GetMapping("getSystemSetting")
    public ResponseEntity getSystemSetting(@CurrentUser UserDetailsImpl userDetails) {
        String uid = "";
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
            uid = userDetails.getTown().getUid();
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            uid = userDetails.getArea().getUid();
        }
        RespBody body = systemSettingService.getSystemSettings(userDetails.getLevel(),uid);
        return ResponseEntity.ok(body);
    }

    /**
     * 保存系统配置
     * @param systemSettingDto
     * @return
     */
    @PostMapping("saveSystemSetting")
    public ResponseEntity saveSystemSetting(SystemSettingDto systemSettingDto) {
        RespBody body = systemSettingService.saveSystemSetting(systemSettingDto);
        return ResponseEntity.ok(body);
    }

}
