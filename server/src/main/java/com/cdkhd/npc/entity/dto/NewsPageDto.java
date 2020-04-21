package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Getter
@Setter
public class NewsPageDto extends PageDto {

    private String title;

    private String author;

    private Integer status;

    private Byte level;

    private String uid;

    private String newsType;

    private String newsTypeUid;

    private Integer whereShow;
}
