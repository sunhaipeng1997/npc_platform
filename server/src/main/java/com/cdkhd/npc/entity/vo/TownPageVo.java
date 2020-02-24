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
public class TownPageVo extends BaseVo {

    // 镇名称
    private String name;

    // 镇介绍
    private String description;

    // 包含小组
    private Set<GroupVo> groups = new HashSet<>();

    public static TownPageVo convert(Town town) {
        TownPageVo vo = new TownPageVo();
        BeanUtils.copyProperties(town, vo);
        Set<NpcMemberGroup> groups = town.getNpcMemberGroups();
        if (groups != null && !groups.isEmpty()) {
            vo.setGroups(groups.stream().map(GroupVo::convert).collect(Collectors.toSet()));
        }
        return vo;
    }
}
