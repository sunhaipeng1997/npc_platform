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
@Entity
@Table ( name ="government" )
public class Government extends BaseDomain {

	/**
	 * 1、正常
	   2、锁定
	 */
   	@Column(name = "status" )
	private Byte status = 1;

	/**
	 * 名称
	 */
	@Column(name = "name" )
	private String name;

	/**
	 * 描述
	 */
	@Column(name = "description" )
	private String description;

	//区政府还是镇政府  1镇 2区
	@Column(name = "level")
	private Byte level;

	//政府地址
	@Column(name = "address" )
	private String address;

	//经度
	@Column(name = "longitude" )
	private String longitude;

	//纬度
	@Column(name = "latitude" )
	private String latitude;

	//政府人员关联
	@OneToOne(mappedBy = "government", targetEntity = GovernmentUser.class, fetch = FetchType.LAZY)
	private GovernmentUser governmentUser;

	/**
	 * 所属区
	 */
	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "area", referencedColumnName = "id")
	private Area area;

	/**
	 * 所属镇
	 */
	@OneToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
	private Town town;

	/**
	 * 基本信息id
	 */
   	@Column(name = "create_user" )
	private String createUser;

	/**
	 * 基本信息id
	 */
   	@Column(name = "update_user" )
	private String updateUser;

}
