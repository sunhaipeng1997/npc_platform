package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class PerformancePageDto extends PageDto {

    //履职等级
    private Byte level;

    //状态
    private Byte status;

    //当前所在镇/区
    private String areaUid;
}
