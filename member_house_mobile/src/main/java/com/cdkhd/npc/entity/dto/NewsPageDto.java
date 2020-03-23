package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsPageDto extends PageDto {

    private String title;

    private String author;

    private Integer status;

    private Integer area;

    private String townId;

    private String newsTypeName;

    private Integer whereShow;
}
