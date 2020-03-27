package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationReviewDto extends BaseDto {
    //审核结果为"通过"或者"不通过"
    private boolean pass;

    //审核人的反馈意见
    private String feedback;

    //暂时加这个字段测试
    private String username;

    private Byte level;
}
