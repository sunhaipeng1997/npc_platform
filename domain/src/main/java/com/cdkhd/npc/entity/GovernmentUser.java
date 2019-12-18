package com.cdkhd.npc.entity;

import javax.persistence.*;
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
@Table ( name ="government_user" )
public class GovernmentUser extends BaseDomain {


	/**
	 * 1、正常
            2、锁定

	 */
   	@Column(name = "status" )
	private Integer status;

	/**
	 * 姓名
	 */
   	@Column(name = "name" )
	private String name;

	/**
	 * 电话号码
	 */
   	@Column(name = "mobile" )
	private String mobile;

	/**
	 * 代表证号
	 */
   	@Column(name = "code" )
	private String code;

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

	/**
	 * 账号表id
	 */
	@OneToOne(targetEntity=Account.class, fetch = FetchType.LAZY)
	private Account account;

}
