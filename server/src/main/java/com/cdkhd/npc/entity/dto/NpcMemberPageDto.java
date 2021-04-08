package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class NpcMemberPageDto extends PageDto {

    //代表姓名关键字
    private String name;

    //代表手机号
    private String phone;

    //出生日期开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startAt;

    //出生日期结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endAt;

    //工作单位uid
    private String workUnitUid;

    //职务类型uid
    private String jobType;

    //届期uid
    private String sessionUid;
}
