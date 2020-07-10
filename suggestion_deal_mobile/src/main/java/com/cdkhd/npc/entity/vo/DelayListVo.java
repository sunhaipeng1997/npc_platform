package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.DelaySuggestion;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description
 * @Author  ly
 * @Date 2020-01-07
 */

@Setter
@Getter
public class DelayListVo extends BaseVo {

    //建议标题
    private String title;

    //建议uid
    private String suggestionUid;

    //建议类型
    private String businessName;

    //办理单位处理时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dateTime;

    //政府是否已查看
    private Boolean view;

    //申请单位
    private String unit;

    public static DelayListVo convert(DelaySuggestion delaySuggestion) {
        DelayListVo vo = new DelayListVo();
        vo.setDateTime(delaySuggestion.getApplyTime());
        vo.setUid(delaySuggestion.getUid());
        vo.setSuggestionUid(delaySuggestion.getSuggestion().getUid());
        vo.setCreateTime(delaySuggestion.getCreateTime());
        vo.setBusinessName(delaySuggestion.getSuggestion().getSuggestionBusiness().getName());
        vo.setTitle(delaySuggestion.getSuggestion().getTitle());
        vo.setUnit(delaySuggestion.getUnitSuggestion().getUnit().getName());
        vo.setView(delaySuggestion.getGovView());
        return vo;
    }
}
