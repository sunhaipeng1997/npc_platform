package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class RelationVo extends BaseVo {

    //名称
    private String name;

    //等級
    private Byte level;

    // 包含镇/小组/村
    private List<RelationVo> children;

    public static RelationVo convert(String uid, String name, Byte level, Date createTime) {
        RelationVo vo = new RelationVo();
        vo.setUid(uid);
        vo.setName(name);
        vo.setLevel(level);
        vo.setCreateTime(createTime);
        return vo;
    }
}
