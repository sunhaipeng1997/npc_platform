package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * @创建人
 * @创建时间 2019/12/25
 * @描述
 */
@Getter
@Setter
public class AddSuggestionDto extends BaseDto {

    //建议的标题
    private String title;

    //业务范围
    private String business;

    //建议提出时间
    private Date raiseTime;

    //建议的内容
    private String content;

    private List<String> images;

    private String areaName;

    private String townName;

    //提建议人代表等级
    private Byte level;

    private String transUid;

    private String raiserMobile;

    private Date auditTime;

    private String auditorMobile;

}
