package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

public interface SystemService {

    RespBody getSystemList();

    RespBody cacheSystem(String uid, String systemId);

    RespBody getCacheSystem(String uid);
}
