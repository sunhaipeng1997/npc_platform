package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.enums.ConveyStatusEnum;
import com.cdkhd.npc.enums.GovDealStatusEnum;
import com.cdkhd.npc.enums.UnitTypeEnum;
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
public class ConveyListVo extends BaseVo {

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

    public static ConveyListVo convertSug(ConveyProcess conveyProcess) {
        ConveyListVo vo = new ConveyListVo();
        vo.setDateTime(conveyProcess.getUnitDealTime());
        vo.setUid(conveyProcess.getUid());
        vo.setSuggestionUid(conveyProcess.getSuggestion().getUid());
        vo.setCreateTime(conveyProcess.getCreateTime());
        vo.setBusinessName(conveyProcess.getSuggestion().getSuggestionBusiness().getName());
        vo.setTitle(conveyProcess.getSuggestion().getTitle());
        vo.setUnit(conveyProcess.getUnit().getName());
        vo.setView(conveyProcess.getGovView());
        return vo;
    }
}
