package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.enums.UnitTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
public class CompleteListItemVo extends BaseVo {
    //建议类型
    private String businessName;

    private String title;

    //受理时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date acceptTime;

    private String memberName;

    private String memberMobile;

    //主办建议/协办建议
    private String unitTypeName;

    //办完时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime;

    public static CompleteListItemVo convert(UnitSuggestion unitSuggestion) {
        CompleteListItemVo vo = new CompleteListItemVo();

        BeanUtils.copyProperties(unitSuggestion, vo);

        Suggestion suggestion = unitSuggestion.getSuggestion();
        vo.setBusinessName(suggestion.getSuggestionBusiness().getName());
        vo.setTitle(suggestion.getTitle());
        vo.setMemberName(suggestion.getRaiser().getName());
        vo.setMemberMobile(suggestion.getRaiser().getMobile());
        vo.setUnitTypeName(unitSuggestion.getType()
                .equals(UnitTypeEnum.MAIN_UNIT.getValue()) ? "主办" : "协办");

        return vo;
    }
}
