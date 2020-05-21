package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.SuggestionSetting;
import com.cdkhd.npc.entity.SystemSetting;
import com.cdkhd.npc.entity.dto.SugSettingDto;
import com.cdkhd.npc.vo.RespBody;

public interface SugSettingService {

    //前端调用
    RespBody getSugSettings(Byte level, String uid);

    //保存系统设置
    RespBody saveSugSetting(UserDetailsImpl userDetails, SugSettingDto sugSettingDto);
}
