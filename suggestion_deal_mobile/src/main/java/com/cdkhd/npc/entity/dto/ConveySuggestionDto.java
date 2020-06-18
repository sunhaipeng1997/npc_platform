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
public class ConveySuggestionDto extends LevelDto {

   	//主办单位
	private String mainUnit;

   	//协办单位
    private List<String> coUnits;


}
