package com.cdkhd.npc.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 前端展示下拉菜单所使用的vo
 */
@Getter
@Setter
public class CommonVo {

    private String uid;

    private String name;

    public static CommonVo convert(String uid, String name) {
        CommonVo vo = new CommonVo();
        vo.setUid(uid);
        vo.setName(name);
        return vo;
    }
}
