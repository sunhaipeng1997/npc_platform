package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.Suggestion;

import java.util.Date;

public class ToDealDetailVo {
    //转办时间
    private Date receiveTime;

    //办理单位类型
    private String unitType;

    //转办单位
    private String govName;

    //待办建议
    private Suggestion suggestion;

    public ToDealDetailVo(Date receiveTime, String unitType, String govName, Suggestion suggestion) {
        this.receiveTime = receiveTime;
        this.unitType = unitType;
        this.govName = govName;
        this.suggestion = suggestion;
    }

    public static ToDealDetailVo convert(ConveyProcess cp) {
        String unitType = cp.getType().equals((byte)1) ? "主办单位" : "协办单位";
        return new ToDealDetailVo(cp.getConveyTime(), unitType,
                cp.getGovernmentUser().getGovernment().getName(), cp.getSuggestion());
    }
}
