package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SessionAddDto extends BaseDto {

    //届期名称
    private String name;

    //开始日期
    private Date startDate;

    //结束日期
    private Date endDate;

    //描述
    private String remark;

}
