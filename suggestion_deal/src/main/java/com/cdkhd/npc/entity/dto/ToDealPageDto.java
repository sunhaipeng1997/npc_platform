package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 分页查询待办建议Dto
 */
@Getter
@Setter
public class ToDealPageDto extends PageDto {
    //建议标题
    private String title;

    //建议类型uid
    private String businessUid;

    //开始时间（转办）
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dateStart;

    //结束时间（转办）
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dateEnd;

    //代表姓名
    private String memberName;

    //代表手机号
    private String memberMobile;

    //办理类型（主办的建议 / 协办的建议）
    Byte unitType;
}
