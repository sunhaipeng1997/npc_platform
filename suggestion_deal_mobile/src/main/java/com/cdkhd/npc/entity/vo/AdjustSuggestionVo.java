package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.DelaySuggestion;
import com.cdkhd.npc.entity.SuggestionImage;
import com.cdkhd.npc.enums.UnitTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    //办理单位处理时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date applyDate;


    //调整单位次数
    private Integer conveyTimes;

    // 申请单位
    private String unit;

    //单位类型
    private Byte unitType;
    private String unitTypeName;

    // 业务类型
    private String business;

    //是否查看
    private Boolean view;

    //建议内容
    private String content;

    //建议图片
    private List<String> images;

    public static AdjustSuggestionVo convert(ConveyProcess conveyProcess) {
        AdjustSuggestionVo vo = new AdjustSuggestionVo();
        BeanUtils.copyProperties(conveyProcess,vo);
        vo.setTitle(conveyProcess.getSuggestion().getTitle());
        vo.setUnit(conveyProcess.getUnit().getName());
        vo.setUnitType(conveyProcess.getType());
        vo.setUnitTypeName(UnitTypeEnum.getName(conveyProcess.getType()));
        vo.setView(conveyProcess.getGovView());
        vo.setApplyDate(conveyProcess.getUnitDealTime());
        vo.setBusiness(conveyProcess.getSuggestion().getSuggestionBusiness().getName());
        vo.setReason(conveyProcess.getRemark());
        vo.setContent(conveyProcess.getSuggestion().getContent());
        vo.setImages(conveyProcess.getSuggestion().getSuggestionImages().stream().map(SuggestionImage::getUrl).collect(Collectors.toList()));
        return vo;
    }
}
