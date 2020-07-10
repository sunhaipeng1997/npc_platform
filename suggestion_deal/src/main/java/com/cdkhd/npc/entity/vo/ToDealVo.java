package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
public class ToDealVo extends BaseVo {
    //转办时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date receiveTime;

    //办理单位类型
    private Byte unitType;

    //办理类型名称 主办单位/协办单位
    private String unitTypeName;

    //单位名称
//    private String unitName;

    //转办政府
    private String govName;

    //建议详情
    private SuggestionVo suggestion;

    public static ToDealVo convert(ConveyProcess cp) {
        ToDealVo vo = new ToDealVo();

        vo.setReceiveTime(cp.getConveyTime());
        vo.setUnitType(cp.getType());
        String unitTypeName = cp.getType().equals((byte)1) ? "主办单位" : "协办单位";
        vo.setUnitTypeName(unitTypeName);
        vo.setGovName(cp.getGovernmentUser().getGovernment().getName());
        vo.setSuggestion(SuggestionVo.convert(cp.getSuggestion()));

        return vo;
    }
}
