package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.DelaySuggestion;
import com.cdkhd.npc.entity.SuggestionImage;
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
public class DelaySuggestionVo extends BaseVo {


    //标题
    private String title;

    //申请原因
    private String reason;

    //延期次数次数
    private Integer delayTimes;

    //预计办理时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectDate;

    //预计延期时间
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date applyTime;

    // 申请单位
    private String unit;

    // 业务类型
    private String business;

    //是否查看
    private Boolean view;

    //建议内容
    private String content;

    //建议图片
    private List<String> images;

    public static DelaySuggestionVo convert(DelaySuggestion delaySuggestion) {
        DelaySuggestionVo vo = new DelaySuggestionVo();
        BeanUtils.copyProperties(delaySuggestion,vo);
        vo.setTitle(delaySuggestion.getSuggestion().getTitle());
        vo.setUnit(delaySuggestion.getUnitSuggestion().getUnit().getName());
        vo.setView(delaySuggestion.getGovView());
        vo.setExpectDate(delaySuggestion.getSuggestion().getExpectDate());
        vo.setBusiness(delaySuggestion.getSuggestion().getSuggestionBusiness().getName());
        vo.setContent(delaySuggestion.getSuggestion().getContent());
        vo.setImages(delaySuggestion.getSuggestion().getSuggestionImages().stream().map(SuggestionImage::getUrl).collect(Collectors.toList()));
        return vo;
    }
}
