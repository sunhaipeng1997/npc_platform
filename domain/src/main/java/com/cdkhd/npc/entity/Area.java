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
@ToString
@Entity
@Table ( name ="area" )
public class Area extends BaseDomain {

   	@Column(name = "name" )
	private String name;

   	@Column(name = "remark" )
	private String remark;

   	@Column(name = "status" )
	private Byte status = StatusEnum.ENABLED.getValue();

    @OneToMany(targetEntity = Town.class, mappedBy = "area", orphanRemoval = true)
	private Set<Town> towns = new HashSet<>();

    @OneToMany(targetEntity = Session.class, mappedBy = "area", orphanRemoval = true)
    private Set<Session> sessions = new HashSet<>();

    @OneToMany(targetEntity = WorkStation.class, mappedBy = "area", orphanRemoval = true)
    private Set<WorkStation> workStations = new HashSet<>();

	@OneToMany(targetEntity = Voter.class, mappedBy = "area", orphanRemoval = true)
	private Set<Voter> voters = new HashSet<>();

}
