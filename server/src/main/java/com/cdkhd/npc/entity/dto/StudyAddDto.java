package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.dto.PageDto;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class StudyAddDto extends BaseDto {

    //学习资料名称
    private String name;

    //类型
    private String studyType;

   	//学习资料id
    private String url;

    //备注
    private String remark;
}
