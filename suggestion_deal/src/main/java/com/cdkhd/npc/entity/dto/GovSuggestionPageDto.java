package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description
 * @Author  ly
 * @Date 2019-01-07
 */

@Setter
@Getter
public class GovSuggestionPageDto extends PageDto {

   	//标题
	private String title;

   	//类型
    private String business;

   	//开始时间（审核）
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date dateStart;

    //结束时间（审核）
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date dateEnd;

    //代表信息
    private String member;

    //代表手机号
    private String mobile;

    //办理单位
    private String unit;

    //建议状态
    private String status;

    //查询类型  1、待转办建议  2、申请调整单位的建议  3、申请延期的建议  4、办理中的建议  5、已办完的建议  6、已办结的建议
    private Byte searchType;


}
