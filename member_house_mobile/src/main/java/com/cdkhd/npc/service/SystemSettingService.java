package com.cdkhd.npc.service;

import com.cdkhd.npc.vo.RespBody;

public interface SystemSettingService {

    //前端调用
    RespBody getSystemSettings(Byte level, String uid);

}
