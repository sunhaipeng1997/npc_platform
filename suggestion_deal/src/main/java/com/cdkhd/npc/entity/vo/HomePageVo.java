package com.cdkhd.npc.entity.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * @描述 意见管理后端分页查询
 */
@Setter
@Getter
public class HomePageVo {

    //本月新增建议
    private Integer newSug;

    //本月办结的建议
    private Integer completedSug;

    //所有办理中的建议
    private Integer dealingSug;

}
