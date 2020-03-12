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
@ToString
@Entity
@Table(name = "town")
public class Town extends BaseDomain {

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "status")
	private Byte status;

	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    private Area area;

    @OneToMany(targetEntity = NpcMemberGroup.class, mappedBy = "town", orphanRemoval = true)
	private Set<NpcMemberGroup> npcMemberGroups = new HashSet<>();

    @OneToMany(targetEntity = Village.class, mappedBy = "town", orphanRemoval = true)
    private Set<Village> villages = new HashSet<>();

    @OneToMany(targetEntity = WorkStation.class, mappedBy = "town", orphanRemoval = true)
    private Set<WorkStation> workStations = new HashSet<>();

    @OneToMany(targetEntity = Session.class, fetch = FetchType.LAZY)
    private Set<Session> sessions = new HashSet<>();

    @OneToMany(targetEntity = NpcMember.class, mappedBy = "town", orphanRemoval = true)
    private Set<NpcMember> npcMembers = new HashSet<>();

}
