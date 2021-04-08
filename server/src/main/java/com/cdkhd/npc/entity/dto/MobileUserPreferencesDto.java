package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MobileUserPreferencesDto extends BaseDto {

    private String shortcutAction;

    private String newsStyle;
}
