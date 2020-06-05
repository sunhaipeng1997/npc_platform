package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.DelaySuggestion;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description
 * @Author  ly
 * @Date 2020-01-07
 */

@Setter
@Getter
public class AdjustSuggestionVo extends BaseVo {

    //标题
    private String title;

    //申请原因
    private String reason;

    //调整单位次数
    private Integer conveyTimes;

    // 申请单位
    private String unit;

    // 业务类型
    private String business;

    //是否查看
    private Boolean view;

    public static AdjustSuggestionVo convert(ConveyProcess conveyProcess) {
        AdjustSuggestionVo vo = new AdjustSuggestionVo();
        BeanUtils.copyProperties(conveyProcess,vo);
        vo.setTitle(conveyProcess.getSuggestion().getTitle());
        vo.setUnit(conveyProcess.getUnit().getName());
        vo.setView(conveyProcess.getGovView());
        vo.setBusiness(conveyProcess.getSuggestion().getSuggestionBusiness().getName());
        return vo;
    }
}
