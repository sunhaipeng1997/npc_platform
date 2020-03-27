package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

public interface MenuService {

    RespBody getMenus(MobileUserDetailsImpl userDetails, String system, Byte level);

    RespBody countUnRead(MobileUserDetailsImpl userDetails, Byte level);
}
