package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
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

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "name")
	private String name;

	@Column(name = "remark")
	private String remark;

	@Column(name = "status")
	private Integer status;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    @OneToMany(targetEntity = NpcMemberGroup.class, orphanRemoval = true)
	@JoinColumn(name = "town", referencedColumnName = "id")
	private Set<NpcMemberGroup> npcMemberGroups = new HashSet<>();

    @OneToMany(targetEntity = Village.class, mappedBy = "town", orphanRemoval = true)
    private Set<Village> villages = new HashSet<>();

    @OneToMany(targetEntity = Session.class, orphanRemoval = true)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Set<Session> sessions = new HashSet<>();

}
