package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;

@Getter
@Setter
public class SugSettingDto extends BaseDto {

    //办理期限（单位：天）
    private Integer expectDate;

    //临期提醒（单位：天）
    private Integer deadline;

    //催办频率（单位：天）
    private Integer urgeFre;



}
