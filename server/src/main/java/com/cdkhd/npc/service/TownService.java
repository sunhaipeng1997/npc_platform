package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.TownAddDto;
import com.cdkhd.npc.entity.dto.TownPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface TownService {

    RespBody page(UserDetailsImpl userDetails, TownPageDto townPageDto);

    RespBody details(String uid);

    RespBody add(UserDetailsImpl userDetails, TownAddDto townAddDto);

    RespBody update(UserDetailsImpl userDetails, TownAddDto townAddDto);

    RespBody delete(String uid);

    RespBody subTownsList(UserDetailsImpl userDetails);

}
