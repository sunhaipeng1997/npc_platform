package com.cdkhd.npc.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AnalysisVo {

    //名称
    private String name;

    //总计
    private int count;

    //子项统计
    private List<AnalysisVo> analysisVoList;

}
