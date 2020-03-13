package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

public interface MenuService {

    RespBody getMenus(UserDetailsImpl userDetails, String system, Byte level);

    RespBody countUnRead(UserDetailsImpl userDetails, Byte level);
}
