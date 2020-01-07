package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.entity.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date workAtStart;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date workAtEnd;

    //代表信息
    private String name;

    private String mobile;

}
