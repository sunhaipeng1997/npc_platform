package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class NpcMemberVo extends BaseVo {

    //代表姓名
    private String name;

    //代表性别
    private String gender;

    //代表号
    private String code;

    //手机号
    private String mobile;

    public static NpcMemberVo convert(NpcMember npcMember) {
        NpcMemberVo vo = new NpcMemberVo();
        BeanUtils.copyProperties(npcMember, vo);
        return vo;
    }
}
