package com.cdkhd.npc.entity.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */
@Getter
@Setter
public class PerformanceTypeAddDto {

    private String uid;

    //类型名称
	private String name;

	//备注
    private String remark;

}
