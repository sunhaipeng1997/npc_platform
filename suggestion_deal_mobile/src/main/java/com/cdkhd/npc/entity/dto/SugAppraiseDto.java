package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;
//接受建议办理结果dto
@Getter
@Setter
public class SugAppraiseDto extends BaseDto {

    //办理结果评分
    private Byte result;

    //办理态度评分
    private Byte attitude;

    //原因
    private String reason;

    //目前代表等级
    private Byte level;
}
