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
@Table ( name ="dictionary" )
public class Dictionary extends BaseDomain {

	/**
	 * 姓名
	 */
   	@Column(name = "name" )
	private String name;

	/**
	 * 电话号码
	 */
   	@Column(name = "type" )
	private String type;

	/**
	 * 1、正常
            2、锁定

	 */
   	@Column(name = "status" )
	private Integer status;

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "create_time" )
	private Date createTime;

	/**
	 * 基本信息id
	 */
   	@Column(name = "create_user" )
	private String createUser;

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "update_tTime" )
	private Date updateTTime;

	/**
	 * 基本信息id
	 */
   	@Column(name = "update_user" )
	private String updateUser;

   	@Column(name = "description" )
	private String description;

   	@Column(name = "sequence" )
	private Integer sequence;

   	@Column(name = "del" )
	private Integer del;

}
