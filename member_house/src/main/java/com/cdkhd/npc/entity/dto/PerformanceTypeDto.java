package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */
@Getter
@Setter
public class PerformanceTypeDto extends PageDto {

    //类型名称
	private String name;

    private Byte status;

}
