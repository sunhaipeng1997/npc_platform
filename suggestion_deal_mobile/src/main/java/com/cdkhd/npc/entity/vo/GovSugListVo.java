package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.enums.GovSugTypeEnum;
import com.cdkhd.npc.enums.UnitTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

/**
 * 查询建议列表项Vo
 */

@Getter
@Setter
public class GovSugListVo extends BaseVo {
    //建议标题
    private String title;

    //时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dateTime;

    //建议类型
    private String business;
    private String businessName;

    //是否未读
    private String raiser;

    //是否未读
    private Boolean view;

    //建议封面图url
    private String coverUrl;

    //主办单位
    private String mainUnit;

    //协办单位
    private String coUnit;

    //组装待审核列表
    public static GovSugListVo convert(Suggestion suggestion,Byte searchType) {
        GovSugListVo vo = new GovSugListVo();
        BeanUtils.copyProperties(suggestion,vo);
        if (searchType.equals(GovSugTypeEnum.WAIT_DEAL_SUG.getValue())) {//待办建议
            vo.setDateTime(suggestion.getAuditTime());//审核日期
        }else if (searchType.equals(GovSugTypeEnum.DEALING_SUG.getValue())){//办理中的建议
            vo.setDateTime(suggestion.getExpectDate());//预计办完时间
        }else if (searchType.equals(GovSugTypeEnum.FINISH_SUG.getValue())){//办完的的建议
            vo.setDateTime(suggestion.getFinishTime());//办完日期
        }else if (searchType.equals(GovSugTypeEnum.ACCOMPLISHED_SUG.getValue())){//办结的建议
            vo.setDateTime(suggestion.getAccomplishTime());//办结日期
        }
        vo.setRaiser(suggestion.getRaiser().getName());
        vo.setView(suggestion.getGovView());
        vo.setBusiness(suggestion.getSuggestionBusiness().getUid());
        vo.setBusinessName(suggestion.getSuggestionBusiness().getName());
        if (null != suggestion.getUnit()){
            vo.setMainUnit(suggestion.getUnit().getName());
        }
        if (CollectionUtils.isNotEmpty(suggestion.getUnitSuggestions())){//协办单位
            StringJoiner stringJoiner = new StringJoiner("、");
            for (UnitSuggestion unitSuggestion : suggestion.getUnitSuggestions()) {
                if (unitSuggestion.getType().equals(UnitTypeEnum.CO_UNIT.getValue())){
                    stringJoiner.add(unitSuggestion.getUnit().getName());
                }
            }
        }

        vo.setCoverUrl(getCoverUrl(suggestion));
        return vo;
    }

    private static String getCoverUrl(Suggestion suggestion) {
        if (CollectionUtils.isNotEmpty(suggestion.getSuggestionImages())) {
            List<SuggestionImage> imageList = new ArrayList<>(suggestion.getSuggestionImages());
            imageList.sort(Comparator.comparing(SuggestionImage::getId));
            return imageList.get(0).getUrl();
        }
        return "";
    }
}
