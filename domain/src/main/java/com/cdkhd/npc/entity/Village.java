package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="village" )
public class Village extends BaseDomain {

   	@Column(name = "introduction" )
	private String introduction;

   	@Column(name = "name" )
	private String name;

   	@ManyToOne(targetEntity = NpcMemberGroup.class, fetch = FetchType.LAZY)
	private NpcMemberGroup npcMemberGroup;

   	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
	private Town town;

}
