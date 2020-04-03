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

    public static LevelVo convert(String uid, String name, Byte level) {
        LevelVo vo = new LevelVo();
        vo.setUid(uid);
        vo.setName(name);
        vo.setLevel(level);
        return vo;
    }
}
