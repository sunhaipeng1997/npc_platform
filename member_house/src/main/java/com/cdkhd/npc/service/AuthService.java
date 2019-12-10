package com.cdkhd.npc.service;

import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.vo.RespBody;

public interface AuthService {
    RespBody getCode(String username);

    RespBody login(UsernamePasswordDto upDto);
}
