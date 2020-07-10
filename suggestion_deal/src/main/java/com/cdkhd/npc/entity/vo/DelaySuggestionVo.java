package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.DelaySuggestion;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.enums.ConveyStatusEnum;
import com.cdkhd.npc.enums.GovDealStatusEnum;
import com.cdkhd.npc.enums.UnitTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * @Description
 * @Author  ly
 * @Date 2020-01-07
 */

@Setter
@Getter
public class DelaySuggestionVo extends BaseVo {

    //是否同意
    private Boolean accept;

    //申请原因
    private String reason;

    //延期次数次数
    private Integer delayTimes;

    //预计延期时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applyTime;

    //审批备注
    private String remark;

    //政府审批的实际延期时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date delayTime;

    //预计完成时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectDate;

    // 建议
    private SuggestionVo suggestionVo;

    // 单位办理情况
    private UnitSugDetailVo unitSugDetailVo;

    public static DelaySuggestionVo convert(DelaySuggestion delaySuggestion) {
        DelaySuggestionVo vo = new DelaySuggestionVo();
        BeanUtils.copyProperties(delaySuggestion,vo);
        vo.setExpectDate(delaySuggestion.getSuggestion().getExpectDate());
        vo.setSuggestionVo(SuggestionVo.convert(delaySuggestion.getSuggestion()));
        vo.setUnitSugDetailVo(UnitSugDetailVo.convert(delaySuggestion.getUnitSuggestion()));
        return vo;
    }
}
