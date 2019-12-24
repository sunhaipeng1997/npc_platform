package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.entity.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class PerformanceDto extends PageDto {

   	//标题
	private String title;

   	//类型
    private String performanceType;

   	//履职时间
	private Date workAtStart;
	private Date workAtEnd;

    //代表信息
    private String npcMember;

}
