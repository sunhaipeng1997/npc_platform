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
@Table ( name ="area" )
public class Area extends BaseDomain {

   	@Column(name = "name" )
	private String name;

   	@Column(name = "remark" )
	private String remark;

   	@Column(name = "status" )
	private Integer status;


    @OneToMany(targetEntity = Town.class, orphanRemoval = true)
	@JoinColumn(name = "area", referencedColumnName = "id")
	private Set<Town> towns = new HashSet<>();

    @OneToMany(targetEntity = Session.class, orphanRemoval = true)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Set<Session> sessions = new HashSet<>();

}
