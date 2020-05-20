package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

public interface IndexService {
    RespBody getIdentityInfo(MobileUserDetailsImpl userDetails);

    RespBody getMenus(MobileUserDetailsImpl userDetails, Byte role, Byte level);
}
