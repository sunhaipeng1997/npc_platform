package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.VillageAddDto;
import com.cdkhd.npc.entity.dto.VillagePageDto;
import com.cdkhd.npc.vo.RespBody;

public interface VillageService {

    RespBody findVillage(UserDetailsImpl userDetails, VillagePageDto villagePageDto);

    RespBody addOrUpdateVillage(UserDetailsImpl userDetails, VillageAddDto villagePageDto);

    RespBody deleteVillage(String uid);

    RespBody optional(UserDetailsImpl userDetails);

    RespBody modifiable(UserDetailsImpl userDetails, String uid);

}
