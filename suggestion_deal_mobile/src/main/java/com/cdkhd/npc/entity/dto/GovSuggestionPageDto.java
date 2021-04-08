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

    //等级
    private Byte level;

    //查询类型  1、待转办建议  2、申请调整单位的建议  3、申请延期的建议  4、办理中的建议  5、已办完的建议  6、已办结的建议
    private Byte searchType;


}
