package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
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
@Table ( name ="unit" )
public class Unit extends BaseDomain {

	/**
	 * 1、正常
	 * 2、锁定
	 */
   	@Column(name = "status" )
	private Integer status;

	/**
	 * 姓名
	 */
   	@Column(name = "name" )
	private String name;

	/**
	 * 业务
	 */
   	@Column(name = "business" )
	private String business;

	/**
	 * 电话号码
	 */
   	@Column(name = "mobile" )
	private String mobile;

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "create_time" )
	private Date createTime;

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "update_time" )
	private Date updateTime;

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
