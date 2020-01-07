package com.cdkhd.npc.entity.dto;


import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class NewsTypeAddDto extends BaseDto {

    private String name;

    private String remark;

    private Byte status;
}
