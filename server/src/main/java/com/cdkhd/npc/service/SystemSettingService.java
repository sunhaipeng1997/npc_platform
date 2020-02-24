package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.SystemSetting;

public interface SystemSettingService {

    SystemSetting getSystemSetting(UserDetailsImpl userDetails);

}
