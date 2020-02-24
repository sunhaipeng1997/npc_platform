package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.NpcMemberGroup;
import com.cdkhd.npc.entity.Village;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class GroupDetailsVo extends BaseVo {

    // 小组名称
    private String name;

    // 小组介绍
    private String description;

    // 小组成员数
    private int memberCount;

    // 小组成员
    private Set<GroupMemberVo> members;

    //小组包含村数量
    private int villageCount;

    //小组包含村
    private Set<VillageVo> villages = new HashSet<>();

    public static GroupDetailsVo convert(NpcMemberGroup npcMemberGroup) {
        GroupDetailsVo vo = new GroupDetailsVo();

        BeanUtils.copyProperties(npcMemberGroup, vo);

        // 特殊处理
        Set<NpcMember> members = npcMemberGroup.getMembers();
        if (members != null && !members.isEmpty()) {
            vo.setMemberCount(members.size());
            vo.setMembers(members.stream().map(GroupMemberVo::convert).collect(Collectors.toSet()));
        }

        Set<Village> villages = npcMemberGroup.getVillages();
        if(villages!=null&& !villages.isEmpty())
        {
            vo.setVillageCount(villages.size());
            vo.setVillages(villages.stream().map(VillageVo::convert).collect(Collectors.toSet()));
        }
        return vo;
    }
}
