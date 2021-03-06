package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsReviewDto extends BaseDto {

    //审核结果为"通过"或者"不通过"
    private Boolean pass;

    //审核人的反馈意见
    private String feedback;

    private Byte level;
}
