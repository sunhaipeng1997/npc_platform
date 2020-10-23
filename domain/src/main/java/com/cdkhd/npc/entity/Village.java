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
@Table ( name ="village" )
public class Village extends BaseDomain {

   	@Column(name = "introduction" )
	private String introduction;

   	@Column(name = "name" )
	private String name;

   	@ManyToOne(targetEntity = NpcMemberGroup.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_member_group", referencedColumnName = "id")
    private NpcMemberGroup npcMemberGroup;

   	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

	@OneToMany(targetEntity = Voter.class, mappedBy = "village", orphanRemoval = true)
	private Set<Voter> voters = new HashSet<>();

}
