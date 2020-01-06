package com.cdkhd.npc.entity.dto;


import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class NewsTypeAddDto extends BaseDto {

    @NotBlank
    private String name;

    private Integer area;

    @NotNull
    private String town;

    //类型状态
    private String status;

    //类型顺序
    private String sequence;

    private String remark;
}
