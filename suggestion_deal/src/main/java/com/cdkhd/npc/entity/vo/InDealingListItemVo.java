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
public class InDealingListItemVo extends BaseVo {

    private String businessName;

    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date acceptTime;

    private String memberName;

    private String memberMobile;

    //主办建议/协办建议
    private String unitTypeName;

    //截止时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectDate;


    public static InDealingListItemVo convert(UnitSuggestion unitSuggestion) {
        InDealingListItemVo vo = new InDealingListItemVo();

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
