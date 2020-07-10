package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitUserPageDto extends PageDto {

    //姓名
    private String name;

    //登录账号
    private String username;

    //所属单位
    private String unit;

    //性别
    private String gender;

    //联系方式
    private String mobile;

}
