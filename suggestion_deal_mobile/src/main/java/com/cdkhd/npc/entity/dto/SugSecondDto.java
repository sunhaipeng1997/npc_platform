package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

//附议建议dto
@Getter
@Setter
public class SugSecondDto extends BaseDto {

    //附议态度  1：支持  2：不支持
    private Byte addition;

    //当前用户身份  1：镇  2：区
    private Byte level;

}
