package com.cdkhd.npc.api;

import com.cdkhd.npc.annotation.CurrentUser;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.MobileUserPreferencesDto;
import com.cdkhd.npc.service.MobileUserPreferencesService;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/mobile/user_preferences")
public class MobileUserPreferencesApi {

    private MobileUserPreferencesService mobileUserPreferencesService;

    @Autowired
    public MobileUserPreferencesApi(MobileUserPreferencesService mobileUserPreferencesService) {
        this.mobileUserPreferencesService = mobileUserPreferencesService;
    }

    /**
     * 查询用户偏好设置
     * @param userDetails 当前用户
     * @return 查询结果
     */
    @GetMapping
    public ResponseEntity getMobileUserPreferences(@CurrentUser UserDetailsImpl userDetails){
        RespBody body = mobileUserPreferencesService.getMobileUserPreferences(userDetails);
        return ResponseEntity.ok(body);
    }

    /**
     * 更新用户偏好设置
     * @param userDetails 当前用户
     * @param dto 用户偏好配置
     * @return 更新结果
     */
    @PutMapping
    public ResponseEntity updateMobileUserPreferences(@CurrentUser UserDetailsImpl userDetails, MobileUserPreferencesDto dto){
        RespBody body = mobileUserPreferencesService.updateMobileUserPreferences(userDetails,dto);
        return ResponseEntity.ok(body);
    }
}
