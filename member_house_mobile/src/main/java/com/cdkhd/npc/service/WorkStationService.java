package com.cdkhd.npc.service;

import com.cdkhd.npc.vo.RespBody;

public interface WorkStationService {

    RespBody detail(String uid);

    RespBody getWorkStations(String uid, Byte level);
}
