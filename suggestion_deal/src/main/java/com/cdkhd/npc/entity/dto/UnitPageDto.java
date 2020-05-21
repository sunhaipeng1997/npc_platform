package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitPageDto extends PageDto {

    //单位名称
    private String name;

    //状态
    private Byte status;

    //业务
    private String business;
}
