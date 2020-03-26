package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.dto.UidDto;
import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.vo.RespBody;

public interface AuthService {
    RespBody getCode(String username);

    RespBody login(UsernamePasswordDto upDto);

    RespBody menus(UserDetailsImpl userDetails, UidDto uidDto);
}
