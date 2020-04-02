package com.cdkhd.npc.api;


import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.LevelDto;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.service.StudyService;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/mobile/systemSetting")
public class SystemSettingApi {
    private SystemSettingService systemSettingService;

    @Autowired
    public SystemSettingApi(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    /**
     * 获取系统配置
     * @param userDetails
     * @return
     */
    @GetMapping("/getSystemSetting")
    public ResponseEntity getSystemSetting(@CurrentUser UserDetailsImpl userDetails, LevelDto levelDto) {
        String uid = "";
        if (levelDto.getLevel().equals(LevelEnum.TOWN.getValue())){
            uid = userDetails.getTown().getUid();
        }else if (levelDto.getLevel().equals(LevelEnum.AREA.getValue())){
            uid = userDetails.getArea().getUid();
        }
        RespBody body = systemSettingService.getSystemSettings(levelDto.getLevel(),uid);
        return ResponseEntity.ok(body);
    }

}
