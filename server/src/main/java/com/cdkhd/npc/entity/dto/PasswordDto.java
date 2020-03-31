package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordDto extends BaseDto {

    //旧密码
    private String oldPwd;

    //新密码
    private String newPwd;

    //确认新密码
    private String confirmPwd;
}
