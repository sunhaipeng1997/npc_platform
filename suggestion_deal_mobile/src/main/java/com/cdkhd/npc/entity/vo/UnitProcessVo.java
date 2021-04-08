package com.cdkhd.npc.entity.vo;
/*
* 办理单位办理流程
*
*
* */

import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class UnitProcessVo extends BaseVo {

    //办理单位名称
    private String unitName;

    //办理单位性质名称 主办单位/协办单位
    private String unitTypeName;

    //办理记录列表
    private List<UnitSugDetailVo> unitSugDetailVos;

    //办理单位接受时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+08")
    private Date acceptTime;

}
