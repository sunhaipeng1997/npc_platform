package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class LevelVo extends BaseVo {

    private String name;

    //区域级别
    private Byte level;

    //身份，参见AccountRoleEnum
    private Byte identity;

    //当前所在镇、区
    private String unitName;

    public static LevelVo convert(String uid, String name, Byte level,Byte identity,String unitName) {
        LevelVo vo = new LevelVo();
        vo.setUid(uid);
        vo.setName(name);
        vo.setLevel(level);
        vo.setIdentity(identity);
        vo.setUnitName(unitName);
        return vo;
    }
}
