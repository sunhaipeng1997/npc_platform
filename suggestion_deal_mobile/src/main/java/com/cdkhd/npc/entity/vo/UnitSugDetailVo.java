package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class UnitSugDetailVo extends BaseVo {
    //办理单位性质 主办单位/协办单位
    private String unitType;

    //办理单位接受时间
    @JsonFormat(pattern = "yy-MM-dd", timezone = "GMT+08")
    private Date acceptTime;

    //办理单位办理次数
    private Integer dealTimes;

    //办理单位办完时间
    @JsonFormat(pattern = "yy-MM-dd", timezone = "GMT+08")
    private Date finishTime;

    //办理单位，是否处理完成（办理完成，拒绝完成）
    private Boolean finish;

    //政府处理过程 1 未处理 2 已重新分配 3 无需重新分配
    private Byte status = 1;

    //办理结果 协办单位反馈给主办单位的结果说明
    private ResultVo result;

    //办理流程
    private List<HandleProcessVo> processes = new ArrayList<>();

    //建议详情
    private SugDetailVo suggestion;

    public UnitSugDetailVo(String uid, String unitType, Date acceptTime, Integer dealTimes, Date finishTime, Boolean finish, Byte status, ResultVo result, List<HandleProcessVo> processes, SugDetailVo suggestion) {
        this.setUid(uid);
        this.unitType = unitType;
        this.acceptTime = acceptTime;
        this.dealTimes = dealTimes;
        this.finishTime = finishTime;
        this.finish = finish;
        this.status = status;
        this.result = result;
        this.processes = processes;
        this.suggestion = suggestion;
    }

    public static UnitSugDetailVo convert(UnitSuggestion unitSug) {
        String unitType = unitSug.getType().equals((byte)1) ? "主办单位" : "协办单位";
        List<HandleProcessVo> processVos = unitSug.getProcesses().stream()
                .map(HandleProcessVo::convert)
                .collect(Collectors.toList());

        return new UnitSugDetailVo(unitSug.getUid(), unitType, unitSug.getAcceptTime(),
                unitSug.getDealTimes(), unitSug.getFinishTime(), unitSug.getFinish(),
                unitSug.getStatus(), ResultVo.convert(unitSug.getResult()), processVos,
                SugDetailVo.convert(unitSug.getSuggestion()));
    }
}
