package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.UnitSuggestion;
import com.cdkhd.npc.enums.UnitTypeEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class UnitSugDetailVo extends BaseVo {

    //办理单位名称
    private String unitName;

    //办理单位性质 1 主办 2 协办
    private Byte unitType;

    //办理单位性质名称 主办单位/协办单位
    private String unitTypeName;

    //转办的政府单位名称
    private String govName;

    //办理单位接受时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+08")
    private Date acceptTime;

    //预计办理完成时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+08")
    private Date expectFinishTime;

    //办理单位办完时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+08")
    private Date finishTime;

    //办理结果 协办单位反馈给主办单位的结果说明
    private ResultVo result;

    //办理流程
    private List<HandleProcessVo> processes = new ArrayList<>();

    //建议详情
    private SugDetailVo suggestion;

    public static UnitSugDetailVo convert(UnitSuggestion unitSug) {
        UnitSugDetailVo vo = new UnitSugDetailVo();

        String unitTypeName = UnitTypeEnum.getName(unitSug.getType());
        List<HandleProcessVo> processVos = unitSug.getProcesses().stream().map(HandleProcessVo::convert).collect(Collectors.toList());

        BeanUtils.copyProperties(unitSug, vo);
        vo.setUnitName(unitSug.getUnit().getName());
        vo.setUnitType(unitSug.getType());
        vo.setUnitTypeName(unitTypeName);
        vo.setGovName(unitSug.getGovernmentUser().getGovernment().getName());
        vo.setExpectFinishTime(unitSug.getExpectDate());
        vo.setResult(ResultVo.convert(unitSug.getResult()));
        vo.setProcesses(processVos);
        vo.setSuggestion(SugDetailVo.convert(unitSug.getSuggestion()));

        return vo;
    }
}
