package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.enums.UnitTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @创建人 LiYang
 * @创建时间 2020/05/18
 * @描述 查看建议详情vo
 */
@Getter
@Setter
public class GovSugDetailVo extends BaseVo {

    //建议标题
    private String title;

    //建议类型
    private String business;

    //提出人
    private String raiser;

    // 提出时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date raiseTime;

    //审核人
    private String auditor;

    //审核时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date auditTime;

    //预计办理时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectDate;

    //申请延期时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date applyDelayDate;

    //办完时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date finishTime;

    //办结时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date accomplishTime;

    //建议的内容
    private String content;

    //主办单位
    private String mainUnit;

    //协办单位
    private String coUnit;

    //图片路径
    private List<String> images;

    //办理情况
    private List<UnitSugDetailVo> unitSugDetailVo;

    //建议结果
    private ResultVo resultVo;

    //评价
    private AppraiseVo appraiseVo;

    //附议人
    private List<String> seconded;

    public static GovSugDetailVo convert(Suggestion suggestion) {
        GovSugDetailVo vo = new GovSugDetailVo();
        BeanUtils.copyProperties(suggestion, vo);
        vo.setBusiness(suggestion.getSuggestionBusiness().getName());
        vo.setRaiser(suggestion.getRaiser().getName());
        vo.setAuditor(suggestion.getAuditor().getName());
        vo.setImages(suggestion.getSuggestionImages().stream().map(SuggestionImage::getUrl).collect(Collectors.toList()));
        if (suggestion.getUnit() != null) {
            vo.setMainUnit(suggestion.getUnit().getName());
        }
        if (suggestion.getUnitSuggestions() != null) {
            StringJoiner stringJoiner = new StringJoiner("、");
            for (UnitSuggestion unitSuggestion : suggestion.getUnitSuggestions()) {
                if (unitSuggestion.getType().equals(UnitTypeEnum.CO_UNIT.getValue())){
                    stringJoiner.add(unitSuggestion.getUnit().getName());
                }
            }
            vo.setCoUnit(stringJoiner.toString());
        }
        if (CollectionUtils.isNotEmpty(suggestion.getUnitSuggestions())){
            vo.setUnitSugDetailVo(suggestion.getUnitSuggestions().stream().map(UnitSugDetailVo::convert).collect(Collectors.toList()));
        }
        if (suggestion.getResult() != null){
            vo.setResultVo(ResultVo.convert(suggestion.getResult()));
        }
        if (suggestion.getAppraise() != null){
            vo.setAppraiseVo(AppraiseVo.convert(suggestion.getAppraise()));
        }
        if (CollectionUtils.isNotEmpty(suggestion.getSecondedSet())){
            vo.setSeconded(suggestion.getSecondedSet().stream().map(sec -> sec.getNpcMember().getName()).collect(Collectors.toList()));
        }
        return vo;
    }
}
