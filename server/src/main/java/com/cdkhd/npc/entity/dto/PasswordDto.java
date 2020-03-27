package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordDto extends BaseDto {

    //旧密码
    private String oldPwd;

    //确认旧密码
    private String confirmOld;

    //新密码
    private String newPwd;
}
