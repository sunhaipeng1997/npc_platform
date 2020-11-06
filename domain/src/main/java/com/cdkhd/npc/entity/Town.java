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
@Table(name = "town")
public class Town extends BaseDomain {

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "status")
	private Byte status;

	//逻辑删除
    @Column(name = "is_del")
    private Boolean isDel = false;

	//1 镇 2 街道
    @Column(name = "type")
    private Byte type;

    //是否 展示
    @Column(name = "is_show")
    private Boolean isShow = true;

    @OneToOne(mappedBy = "town", targetEntity = Government.class, fetch = FetchType.LAZY)
    private Government government;

    @OneToMany(targetEntity = NpcMemberGroup.class, mappedBy = "town", orphanRemoval = true,fetch = FetchType.LAZY)
	private Set<NpcMemberGroup> npcMemberGroups = new HashSet<>();

    @OneToMany(targetEntity = BackgroundAdmin.class, mappedBy = "town", orphanRemoval = true,fetch = FetchType.LAZY)
    private Set<BackgroundAdmin> backgroundAdmins = new HashSet<>();

    @OneToMany(targetEntity = Village.class, mappedBy = "town", orphanRemoval = true,fetch = FetchType.LAZY)
    private Set<Village> villages = new HashSet<>();

    @OneToMany(targetEntity = WorkStation.class, mappedBy = "town", orphanRemoval = true,fetch = FetchType.LAZY)
    private Set<WorkStation> workStations = new HashSet<>();

    @OneToMany(targetEntity = Session.class, fetch = FetchType.LAZY)
    private Set<Session> sessions = new HashSet<>();

    @OneToMany(targetEntity = NpcMember.class, mappedBy = "town", orphanRemoval = true,fetch = FetchType.LAZY)
    private Set<NpcMember> npcMembers = new HashSet<>();

    @OneToMany(targetEntity = Voter.class, mappedBy = "town", orphanRemoval = true,fetch = FetchType.LAZY)
    private Set<Voter> voters = new HashSet<>();

    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

}
