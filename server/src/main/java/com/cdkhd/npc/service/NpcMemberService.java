package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.vo.RespBody;

public interface NpcMemberService {

    RespBody pageOfNpcMembers(UserDetailsImpl userDetails, NpcMemberPageDto pageDto);

    RespBody addNpcMember(UserDetailsImpl userDetails, NpcMemberAddDto dto);

    RespBody updateNpcMember(UserDetailsImpl userDetails, NpcMemberAddDto dto);

}
