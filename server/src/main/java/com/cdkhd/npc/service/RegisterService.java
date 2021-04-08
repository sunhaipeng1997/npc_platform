package com.cdkhd.npc.service;

import com.cdkhd.npc.entity.dto.PhoneNumberDto;
import com.cdkhd.npc.entity.dto.TypeDto;
import com.cdkhd.npc.entity.dto.UserInfoDto;
import com.cdkhd.npc.vo.RespBody;

public interface RegisterService {

    RespBody getRelations(TypeDto typeDto);

    RespBody getVerificationCode(PhoneNumberDto dto);

    RespBody register(UserInfoDto userInfoDto);
}
