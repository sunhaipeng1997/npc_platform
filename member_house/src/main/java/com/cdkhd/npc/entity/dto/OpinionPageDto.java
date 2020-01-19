package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @描述 意见管理后端分页查询
 */
@Setter
@Getter
public class OpinionPageDto extends PageDto {

    //接受代表名称
    private String memberName;

    //提出人手机号
    private String mobile;

    //接受代表所属机构uid
    private String uid;

    //提出时间开始
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dateStart;

    //提出时间结束
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dateEnd;

}
