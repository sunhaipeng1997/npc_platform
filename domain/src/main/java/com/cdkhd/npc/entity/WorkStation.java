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
@Table ( name ="work_station" )
public class WorkStation extends BaseDomain {

	@Column(name = "name" )
	private String name;

   	@Column(name = "address" )
	private String address;

   	//工作站图片
   	@Column(name = "avatar" )
	private String avatar;

   	@Column(name = "description" )
	private String description;

   	//是否可用
   	@Column(name = "enabled" )
	private Boolean enabled = true;

   	@Column(name = "latitude" )
	private String latitude;

   	//联系人
   	@Column(name = "linkman" )
	private String linkman;

   	@Column(name = "longitude" )
	private String longitude;

   	//联系电话
   	@Column(name = "telephone" )
	private String telephone;

   	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

   	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

	/**
	 * 等级
    1、镇上工作站
    2、区上工作站
	 */
   	@Column(name = "level" )
	private Byte level;

}
