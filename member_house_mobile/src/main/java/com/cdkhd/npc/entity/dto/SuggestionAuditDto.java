package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuggestionAuditDto extends BaseDto {

    //是否接受 1 接受 2 拒绝
    private Byte accept;

    //审核原因
    private String reason;

    //等级
    private Byte level;
}
