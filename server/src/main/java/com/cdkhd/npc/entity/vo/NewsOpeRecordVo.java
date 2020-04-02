package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NewsOpeRecord;
import com.cdkhd.npc.entity.NotificationOpeRecord;
import com.cdkhd.npc.enums.NewsStatusEnum;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class NewsOpeRecordVo  extends BaseVo {
    private Integer originalStatus;
    private String originalStatusName;

    private Integer resultStatus;
    private String resultStatusName;

    private String feedback;

    //操作人姓名
    private String operatorName;

    public static NewsOpeRecordVo convert(NewsOpeRecord opeRecord) {
        NewsOpeRecordVo vo = new NewsOpeRecordVo();

        BeanUtils.copyProperties(opeRecord, vo);
        vo.setOriginalStatusName(NewsStatusEnum.values()[opeRecord.getOriginalStatus()].getName());
        vo.setResultStatusName(NewsStatusEnum.values()[opeRecord.getResultStatus()].getName());

        vo.setOperatorName(opeRecord.getOperator().getName());
        return vo;
    }
}
