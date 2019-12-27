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
@Table ( name ="work_station" )
public class WorkStation extends BaseDomain {


   	@Column(name = "create_at" )
	private Date createAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "address" )
	private String address;

   	@Column(name = "avatar" )
	private String avatar;

   	@Column(name = "description" )
	private String description;

   	@Column(name = "enabled" )
	private Boolean enabled;

   	@Column(name = "latitude" )
	private String latitude;

   	@Column(name = "linkman" )
	private String linkman;

   	@Column(name = "longitude" )
	private String longitude;

   	@Column(name = "name" )
	private String name;

   	@Column(name = "telephone" )
	private String telephone;

   	@Column(name = "town_id" )
	private String townId;

	/**
	 * 等级
            1、镇上工作站
            2、区上工作站
	 */
   	@Column(name = "level" )
	private Byte level;

}
