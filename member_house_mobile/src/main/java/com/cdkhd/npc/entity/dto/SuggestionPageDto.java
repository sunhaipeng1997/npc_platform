package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

/**
 * @创建人
 * @创建时间 2019/12/24
 * @描述
 */
@Setter
@Getter
public class SuggestionPageDto extends PageDto {

    //建议状态 1：全部  2：未审核  3：已审核
    private Byte status;

    private String town;
}
