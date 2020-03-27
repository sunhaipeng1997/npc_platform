package com.cdkhd.npc.service;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.PasswordDto;
import com.cdkhd.npc.entity.dto.UsernamePasswordDto;
import com.cdkhd.npc.vo.RespBody;

public interface AuthService {
    RespBody getCode(String username);

    RespBody login(UsernamePasswordDto upDto);

    RespBody auth(String nickName, String token,String encryptedData, String iv);

    String accessToken(String code, String state);

    RespBody menus(UserDetailsImpl userDetails, BaseDto baseDto);

    RespBody updatePwd(UserDetailsImpl userDetails, PasswordDto passwordDto);
}
