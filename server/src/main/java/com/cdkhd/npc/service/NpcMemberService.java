package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.NpcMemberAddDto;
import com.cdkhd.npc.entity.dto.NpcMemberPageDto;
import com.cdkhd.npc.vo.RespBody;
import org.springframework.web.multipart.MultipartFile;

public interface NpcMemberService {

    RespBody pageOfNpcMembers(UserDetailsImpl userDetails, NpcMemberPageDto pageDto);

    RespBody addOrUpdateNpcMember(UserDetailsImpl userDetails, NpcMemberAddDto dto);

    RespBody deleteNpcMember(String uid);

    RespBody uploadAvatar(UserDetailsImpl userDetails, MultipartFile avatar);

    RespBody getWorkUnits(UserDetailsImpl userDetails);

    RespBody npcMemberList(UserDetailsImpl userDetails);
}
