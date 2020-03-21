package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsTypePageDto extends PageDto {

    //类型名称
    private String name;

    private Byte status;

    private String areaUid;

    private String townUid;
}
