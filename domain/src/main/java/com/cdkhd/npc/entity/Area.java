package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.StatusEnum;
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
@Table ( name ="area" )
public class Area extends BaseDomain {

   	@Column(name = "name" )
	private String name;

   	@Column(name = "remark" )
	private String remark;

   	@Column(name = "status" )
	private Byte status = StatusEnum.ENABLED.getValue();

    @OneToMany(targetEntity = Town.class, mappedBy = "area", orphanRemoval = true,fetch = FetchType.LAZY)
	private Set<Town> towns = new HashSet<>();

    @OneToMany(targetEntity = Session.class, mappedBy = "area", orphanRemoval = true,fetch = FetchType.LAZY)
    private Set<Session> sessions = new HashSet<>();

    @OneToMany(targetEntity = WorkStation.class, mappedBy = "area", orphanRemoval = true,fetch = FetchType.LAZY)
    private Set<WorkStation> workStations = new HashSet<>();

	@OneToMany(targetEntity = Voter.class, mappedBy = "area", orphanRemoval = true,fetch = FetchType.LAZY)
	private Set<Voter> voters = new HashSet<>();

	@OneToMany(targetEntity = NpcMember.class, mappedBy = "area", orphanRemoval = true,fetch = FetchType.LAZY)
	private Set<NpcMember> npcMembers = new HashSet<>();

	@OneToMany(targetEntity = Government.class, mappedBy = "area", orphanRemoval = true,fetch = FetchType.LAZY)
	private Set<Government> governments = new HashSet<>();

	@ManyToMany
	@JoinTable(
			name = "area_systems_mid",
			joinColumns = {
					@JoinColumn(name = "area_id", referencedColumnName = "id")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "systems_id", referencedColumnName = "id")
			}
	)
	private Set<Systems> systems;
}
