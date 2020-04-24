package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
public class MemberCountDto extends PageDto {

    //代表名称
    private String name;

    //工作单位
    private String groupId;

    //履职开始日期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startAt;

    //履职结束日期
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endAt;

}
