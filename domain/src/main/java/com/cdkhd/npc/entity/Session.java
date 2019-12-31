package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

import lombok.Data;
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
@Table ( name ="session" )
public class Session extends BaseDomain {

	/**
	 * 届期名称
	 */
   	@Column(name = "name" )
	private String name;

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "start_time" )
	private Date startTime;

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "end_time" )
	private Date endTime;

	/**
	 * 基本信息id
	 */
   	@Column(name = "create_user" )
	private String createUser;

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
   	@Column(name = "update_user" )
	private String updateUser;

   	@Column(name = "remark" )
	private String remark;

}
