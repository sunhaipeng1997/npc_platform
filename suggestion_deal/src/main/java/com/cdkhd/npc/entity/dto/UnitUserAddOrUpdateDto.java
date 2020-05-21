package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitUserAddOrUpdateDto extends BaseDto {

    //所属单位
    private String unit;

    //工作人员姓名
    private String name;

    //性别
    private Byte gender;

    //联系电话
    private String mobile;

    //登录账号
    private String username;

    //登录密码
    private String password;

    //登录密码确认
    private String confirmPwd;

    // 备注情况
    private String comment;

    //头像
    private String avatar;

}
