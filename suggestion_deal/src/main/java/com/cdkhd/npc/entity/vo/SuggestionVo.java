package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.enums.UnitTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author  ly
 * @Date 2020-01-07
 */

@Setter
@Getter
public class SuggestionVo extends BaseVo {

    //建议内容
	private String content;

   	//审核原因
	private String reason;

   	//状态
	private Byte status;

	//状态名称
	private String statusName;

   	//标题
	private String title;

   	//类型
    private SuggestionBusinessVo suggestionBusiness;

    //业务类型名称
    private String businessName;

    //提出日期
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date raiseTime;

   	//审核时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

   	//转办时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date conveyTime;

    //办理单位接受时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date acceptTime;

    //办理单位完成时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime;

    //建议办结时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date accomplishTime;

    //代表信息
    private String memberName;

    //代表手机号
    private String memberMobile;

    //审核人
    private String auditor;

    //建议回复详情
    private List<SuggestionReplyVo> suggestionReplyVos;

    //建议图片
    private List<String> images;

    //建议等级
    private Byte level;
    private String levelName;

    //单位名称
    private String unitName;
    private String coUitName;

    //转办流程
    private List<ConveyProcessVo> conveyProcessVos;

    //办理流程
    private List<UnitSugDetailVo> unitSugDetailVos;

    //预计办理时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectDate;

    public static SuggestionVo convert(Suggestion suggestion) {
        SuggestionVo vo = new SuggestionVo();
        BeanUtils.copyProperties(suggestion, vo);
        vo.setStatusName(SuggestionStatusEnum.getName(suggestion.getStatus()));
        vo.setMemberName(suggestion.getRaiser().getName());
        vo.setMemberMobile(suggestion.getRaiser().getMobile());
        if (suggestion.getAuditor() != null) {
            vo.setAuditor(suggestion.getAuditor().getName());
        }
        else {
            vo.setAuditor("未审核");
        }
        vo.setSuggestionBusiness(SuggestionBusinessVo.convert(suggestion.getSuggestionBusiness()));
        vo.setBusinessName(suggestion.getSuggestionBusiness().getName());
        vo.setSuggestionReplyVos(suggestion.getReplies().stream().map(SuggestionReplyVo::convert).collect(Collectors.toList()));
        if (CollectionUtils.isNotEmpty(suggestion.getSuggestionImages())){
            vo.setImages(suggestion.getSuggestionImages().stream().map(SuggestionImage::getUrl).collect(Collectors.toList()));
        }
        vo.setLevelName(LevelEnum.getName(suggestion.getLevel()));
        vo.setUnitName(suggestion.getUnit() != null? suggestion.getUnit().getName():"");
        vo.setConveyProcessVos(suggestion.getConveyProcesses().stream().map(ConveyProcessVo::convert).collect(Collectors.toList()));
        vo.setUnitSugDetailVos(suggestion.getUnitSuggestions().stream().map(UnitSugDetailVo::convertNoSug).collect(Collectors.toList()));
        StringJoiner stringJoiner = new StringJoiner("、");
        for (UnitSuggestion unitSuggestion : suggestion.getUnitSuggestions()) {
            if (unitSuggestion.getType().equals(UnitTypeEnum.CO_UNIT.getValue())) {
                stringJoiner.add(unitSuggestion.getUnit().getName());
            }
        }
        vo.setCoUitName(stringJoiner.toString());
        return vo;
    }

}
