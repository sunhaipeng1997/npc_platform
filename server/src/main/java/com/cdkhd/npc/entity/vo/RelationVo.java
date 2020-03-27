package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RelationVo extends BaseVo {

    //名称
    private String name;

    // 包含镇/小组/村
    private List<RelationVo> children;

    public static RelationVo convert(String uid, String name) {
        RelationVo vo = new RelationVo();
        vo.setUid(uid);
        vo.setName(name);
        return vo;
    }
}
