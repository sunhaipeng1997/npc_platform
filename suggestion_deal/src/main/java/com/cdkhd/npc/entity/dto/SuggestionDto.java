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
public class SuggestionDto extends PageDto {

   	//标题
	private String title;

   	//类型
    private String suggestionBusiness;

   	//开始时间（审核）
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date auditStart;

    //结束时间（审核）
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date auditEnd;

    //代表信息
    private String name;

    //代表手机号
    private String mobile;

    //下属镇uid
    private String townUid;

    //标志位 当为true表示查询与当前用户级别相同的建议，为false表示查询区下属镇的建议
    private boolean flag;

}
