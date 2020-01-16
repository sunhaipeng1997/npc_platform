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
public class OpinionPageDto extends PageDto {

    //接受代表名称
    private String memberName;

    //接受代表手机号
    private String mobile;

    //接受代表所属机构uid
    private String uid;

    //提出时间开始
    private Date dateStart;

    //提出时间结束
    private Date dateEnd;

}
