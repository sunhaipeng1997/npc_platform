package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMemberGroup;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class TownDetailsVo extends BaseVo {

    // 镇名称
    private String name;

    // 镇介绍
    private String description;

    //镇包含小组
    private Set<GroupVo> groupVos = new HashSet<>();

    public static TownDetailsVo convert(Town town) {
        TownDetailsVo vo = new TownDetailsVo();
        BeanUtils.copyProperties(town, vo);
        Set<NpcMemberGroup> npcMemberGroups = town.getNpcMemberGroups();
        if (npcMemberGroups != null && !npcMemberGroups.isEmpty()) {
            vo.setGroupVos(npcMemberGroups.stream().map(GroupVo::convert).collect(Collectors.toSet()));
        }
        return vo;
    }
}
