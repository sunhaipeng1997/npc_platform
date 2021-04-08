package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SuggestionBusinessDto extends PageDto {

    //类型名称
    private String name;

    private Byte status;
}
