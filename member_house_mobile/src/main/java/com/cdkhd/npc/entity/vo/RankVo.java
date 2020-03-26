package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RankVo extends BaseVo {

    //名字
    private String name;

    //数量
    private Integer number;

    public static RankVo convert(String uid, String name,int count) {
        RankVo vo = new RankVo();
        vo.setUid(uid);
        vo.setName(name);
        vo.setNumber(count);
        return vo;
    }

}
