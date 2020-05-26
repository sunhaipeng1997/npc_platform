package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.ConveyProcess;
import com.cdkhd.npc.entity.Suggestion;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ToDealDetailVo extends BaseVo {
    //转办时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date receiveTime;

    //办理单位类型
    private String unitType;

    //转办单位
    private String govName;

    //建议详情
    private SugDetailVo suggestion;

    public ToDealDetailVo(String uid, Date receiveTime, String unitType, String govName, SugDetailVo suggestion) {
        this.setUid(uid);
        this.receiveTime = receiveTime;
        this.unitType = unitType;
        this.govName = govName;
        this.suggestion = suggestion;
    }

    public static ToDealDetailVo convert(ConveyProcess cp) {
        String unitType = cp.getType().equals((byte)1) ? "主办单位" : "协办单位";

        return new ToDealDetailVo(cp.getUid(), cp.getConveyTime(), unitType,
                cp.getGovernmentUser().getGovernment().getName(), SugDetailVo.convert(cp.getSuggestion()));
    }
}
