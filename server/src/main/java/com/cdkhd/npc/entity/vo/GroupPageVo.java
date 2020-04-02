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
public class GroupPageVo extends BaseVo {

    // 小组名称
    private String name;

    // 小组介绍
    private String description;

    // 小组成员数
    private int memberCount;

    // 小组成员
    private Set<GroupMemberVo> members;

    private Set<VillageVo> villages = new HashSet<>();

    private int villageCount;


    public static GroupPageVo convert(NpcMemberGroup npcMemberGroup) {
        GroupPageVo vo = new GroupPageVo();
        BeanUtils.copyProperties(npcMemberGroup, vo);
        // 特殊处理
        int count = 0;
        Set<NpcMember> thisMembers = new HashSet<>();//本届代表
        Set<NpcMember> members = npcMemberGroup.getMembers();
        if (members != null) {
            for (NpcMember member : members) {
//                if (member.getStatus() == 0 && member.getSpecial() == 0) {
                    count++;
                    thisMembers.add(member);
//                }
            }
            vo.setMemberCount(count);
            vo.setMembers(thisMembers.stream().map(GroupMemberVo::convert).collect(Collectors.toSet()));
        }
        Set<Village> villages = npcMemberGroup.getVillages();
        if (villages != null && !villages.isEmpty()) {
            vo.setVillageCount(villages.size());
            vo.setVillages(villages.stream().map(VillageVo::convert).collect(Collectors.toSet()));
        }
        return vo;
    }
}
