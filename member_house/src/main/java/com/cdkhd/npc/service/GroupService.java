package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.GroupAddDto;
import com.cdkhd.npc.entity.dto.GroupPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface GroupService {

    RespBody page(UserDetailsImpl userDetails, GroupPageDto GroupPageDto);

    RespBody details(String uid);

    RespBody add(UserDetailsImpl userDetails, GroupAddDto groupAddDto);

    RespBody update(UserDetailsImpl userDetails, GroupAddDto groupAddDto);

    RespBody delete(String uid);

}
