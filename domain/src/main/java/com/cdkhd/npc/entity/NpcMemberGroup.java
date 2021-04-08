package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@Entity
@Table ( name ="npc_member_group" )
public class NpcMemberGroup extends BaseDomain {

   	@Column(name = "description" )
	private String description;

   	@Column(name = "name" )
	private String name;

    @OneToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
	private NpcMember leader;

    @Column(name = "level" )
    private Byte level;

    @OneToMany(targetEntity = Village.class, mappedBy = "npcMemberGroup", orphanRemoval = true)
    private Set<Village> villages = new HashSet<>();

    //关联区
    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    //关联镇
    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    // 小组成员
    @OneToMany(targetEntity = NpcMember.class, mappedBy = "npcMemberGroup")
    private Set<NpcMember> members = new HashSet<>();

}
