package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.vo.RespBody;

public interface SystemService {

    RespBody getSystemList();

    RespBody cacheSystem(String uid, String systemId);

    //source  1 后台登录    2 小程序登录
    RespBody getCacheSystem(String uid,Byte source);
}
