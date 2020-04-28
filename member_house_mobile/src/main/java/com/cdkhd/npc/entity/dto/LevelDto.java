package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LevelDto extends BaseDto {

    //选中的uid
    private Byte level;

}
