package com.cdkhd.npc.entity.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * @描述 管理员登录建议办理系统首页数据vo
 */
@Setter
@Getter
public class AdminHomePageVo {

    //本月新增建议
    private Integer newSug;

    //本月审核通过的建议
    private Integer auditPassSug;

    //所有审核不通过的建议
    private Integer auditRefuseSug;

}
