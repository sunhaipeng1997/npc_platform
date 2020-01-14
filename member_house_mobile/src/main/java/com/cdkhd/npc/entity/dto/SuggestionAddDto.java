package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * @创建人
 * @创建时间 2019/12/25
 * @描述
 */
@Getter
@Setter
public class SuggestionAddDto extends BaseDto {

    //建议的标题
    private String title;

    //业务范围
    private String business;

    //建议提出时间
    private Date raiseTime;

    //建议的内容
    private String content;

    //接收人主要是主任代表
//    private List<String> receiverId;

//    private String mark;

    private MultipartFile images;

    private String town;

    private Byte Level;

    private String TransUid;

}
