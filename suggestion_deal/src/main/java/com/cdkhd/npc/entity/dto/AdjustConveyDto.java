package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description
 * @Author  ly
 * @Date 2019-01-07
 */

@Setter
@Getter
public class AdjustConveyDto extends BaseDto {

   	//单位uid
	private String unit;

   	//办理单位性质 1 主办  2 协办
    private Byte unitType;

    //政府处理状态 1 已重新分配 2 无需重新分配
    private Byte dealStatus;

    //调整说明
	private String desc;

}
