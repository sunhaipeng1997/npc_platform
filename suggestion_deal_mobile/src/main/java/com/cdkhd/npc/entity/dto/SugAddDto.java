package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Getter
@Setter
public class SugAddDto extends BaseDto {

    //建议的标题
    private String title;

    //业务范围
    private String business;

    //建议提出时间
    private Date raiseTime;

    //建议的内容
    private String content;

    private MultipartFile image;

    private String town;

    //提建议人代表等级
    private Byte level;

    private String transUid;

    //1 存至草稿  2 提交审核
    private Byte status;

}
