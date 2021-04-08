package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.cdkhd.npc.vo.CommonVo;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class MemberListVo extends BaseVo {

    //镇/组 名称
    private String name;

    //单位下的代表成员
    private List<CommonVo> members;

    public static MemberListVo convert(String uid, String name, Set<NpcMember> npcMembers,Byte level) {
        MemberListVo vo = new MemberListVo();
        vo.setUid(uid);
        vo.setName(name);
        vo.setMembers(npcMembers.stream().filter(member-> !member.getIsDel() && member.getStatus().equals(StatusEnum.ENABLED.getValue()) && level.equals(member.getLevel())).map(member ->CommonVo.convert(member.getUid(),member.getName())).sorted(Comparator.comparing(CommonVo::getName)).collect(Collectors.toList()));
        return vo;
    }
}
