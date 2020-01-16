package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @描述 意见管理后端分页查询
 */
@Setter
@Getter
public class StatisticalPageDto extends PageDto {

    //接受代表名称
    private String name;

    //代表所属小组、镇
    private String areaUId;

    //履职时间开始
    private Date dateStart;

    //履职时间结束
    private Date dateEnd;

}
