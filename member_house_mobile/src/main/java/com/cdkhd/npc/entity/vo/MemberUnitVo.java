package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
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

    //是否是当前小组
    private Boolean isCurrent = false;

    public static MemberUnitVo convert(String uid, String name, Byte level, Date createDate,Boolean isCurrent) {
        MemberUnitVo vo = new MemberUnitVo();
        vo.setUid(uid);
        vo.setName(name);
        vo.setLevel(level);
        vo.setCreateTime(createDate);
        vo.setIsCurrent(isCurrent);
        return vo;
    }

    public static MemberUnitVo convert(String uid, String name, Byte level, Date createDate) {
        MemberUnitVo vo = new MemberUnitVo();
        vo.setUid(uid);
        vo.setName(name);
        vo.setLevel(level);
        vo.setCreateTime(createDate);
        return vo;
    }
}
