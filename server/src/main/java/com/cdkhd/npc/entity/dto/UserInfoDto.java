package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class UserInfoDto extends BaseDto {

    //姓名
    private String name;

    //手机号
    private String mobile;

    //所属区
    private String area;

    //所属镇
    private String town;

    //所属村
    private String village;


}
