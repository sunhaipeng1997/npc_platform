package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="npc_member_group" )
public class NpcMemberGroup extends BaseDomain {

   	@Column(name = "create_at" )
	private Date createAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "description" )
	private String description;

   	@Column(name = "name" )
	private String name;

   	@Column(name = "leader_id" )
	private Long leaderId;

   	@Column(name = "town_id" )
	private String townId;

}
