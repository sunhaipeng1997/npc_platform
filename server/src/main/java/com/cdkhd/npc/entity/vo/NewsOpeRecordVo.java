package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NewsOpeRecord;
import com.cdkhd.npc.entity.NotificationOpeRecord;
import com.cdkhd.npc.enums.NewsStatusEnum;

import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class NewsOpeRecordVo  extends BaseVo {
    private Integer originalStatus;
    private String originalStatusName;

    private Integer resultStatus;
    private String resultStatusName;

    private String feedback;

    private String action;

    //操作人姓名
    private String operator;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date opTime;

    public static NewsOpeRecordVo convert(NewsOpeRecord opeRecord) {
        NewsOpeRecordVo vo = new NewsOpeRecordVo();

        BeanUtils.copyProperties(opeRecord, vo);
        vo.setOriginalStatusName(NewsStatusEnum.values()[opeRecord.getOriginalStatus()].getName());
        vo.setResultStatusName(NewsStatusEnum.values()[opeRecord.getResultStatus()].getName());
        return vo;
    }
}
