package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.entity.NpcMemberGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Setter
@Getter
public class GroupAddDto {

    private String uid;

    // 小组名称
    private String name;

    // 小组介绍
    private String description;

    //小组包含的村的uid
    private Set<String> villages= new HashSet<>();


    public NpcMemberGroup convert() {
        NpcMemberGroup group = new NpcMemberGroup();
//        BeanUtils.copyProperties(this, group);
        group.setName(this.getName());
        group.setDescription(this.getDescription());
        return group;
    }
}
