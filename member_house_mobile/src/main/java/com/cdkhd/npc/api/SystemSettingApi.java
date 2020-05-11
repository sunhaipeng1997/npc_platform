package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.service.MenuService;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member_house_mobile/systemSetting")
public class SystemSettingApi {

    private SystemSettingService systemSettingService;

    @Autowired
    public SystemSettingApi(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }


    /**
     * 获取当前用户的菜单
     * @param level
     * @return
     */
    @GetMapping("/getSettings")
    public ResponseEntity getMenus(@CurrentUser MobileUserDetailsImpl userDetails, Byte level) {
        String uid = "";
        if (level.equals(LevelEnum.AREA.getValue())){
            uid = userDetails.getArea().getUid();
        }else if (level.equals(LevelEnum.TOWN.getValue())){
            uid = userDetails.getTown().getUid();
        }
        RespBody body = systemSettingService.getSystemSettings(level,uid);
        return ResponseEntity.ok(body);
    }


}
