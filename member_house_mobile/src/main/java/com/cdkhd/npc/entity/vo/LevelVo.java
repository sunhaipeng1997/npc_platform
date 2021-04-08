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

    //回复列表
    private Byte Level;

    //身份  1.代表  2.选民
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
