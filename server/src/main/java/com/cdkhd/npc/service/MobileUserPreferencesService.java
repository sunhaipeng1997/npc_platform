package com.cdkhd.npc.service;


import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.MobileUserPreferencesDto;
import com.cdkhd.npc.vo.RespBody;

public interface MobileUserPreferencesService {

    RespBody getMobileUserPreferences(UserDetailsImpl userDetails);

    RespBody updateMobileUserPreferences(UserDetailsImpl userDetails, MobileUserPreferencesDto dto);
}
