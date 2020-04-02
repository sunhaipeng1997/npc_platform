package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.entity.dto.SystemSettingDto;
import com.cdkhd.npc.vo.RespBody;

public interface SystemSettingService {

    //系统内部调用，不往前端反
    SystemSetting getSystemSetting(Byte level, String uid);

    //前端调用
    RespBody getSystemSettings(Byte level, String uid);

    //保存系统设置
    RespBody saveSystemSetting(SystemSettingDto systemSettingDto);
}
