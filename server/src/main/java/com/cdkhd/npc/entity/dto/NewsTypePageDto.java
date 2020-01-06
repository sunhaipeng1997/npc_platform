package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsTypePageDto extends PageDto {

    private String uid;

    private String name;

    private Integer area;

    private String town;

    //类型状态
    private String status;

    //类型顺序
    private String sequence;

    private String remark;
}
