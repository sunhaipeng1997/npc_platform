package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
public class MemberUnitVo extends BaseVo {

    private String name;

    private Byte level;

    private List<MemberUnitVo> children;

    public static MemberUnitVo convert(String uid, String name, Byte level) {
        MemberUnitVo vo = new MemberUnitVo();
        vo.setUid(uid);
        vo.setName(name);
        vo.setLevel(level);
        return vo;
    }
}
