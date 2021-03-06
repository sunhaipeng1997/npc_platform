package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author  ly
 * @Date 2019-01-10
 */

@Setter
@Getter
public class VillagePageDto extends PageDto {

   	//村名称
	private String name;

    //小组uid
    private String group;
}
