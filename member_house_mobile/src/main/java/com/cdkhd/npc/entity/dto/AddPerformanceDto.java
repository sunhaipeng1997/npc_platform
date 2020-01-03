package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.dto.PageDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class AddPerformanceDto extends BaseDto {

   	//标题
	private String title;

   	//类型
    private String performanceType;

   	//履职时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date workAt;

    //履职内容
    private String content;

    //履职等级
    private Byte level;

    //图片
    private MultipartFile image;

    //每次提交的uid
    private String transUid;

}
