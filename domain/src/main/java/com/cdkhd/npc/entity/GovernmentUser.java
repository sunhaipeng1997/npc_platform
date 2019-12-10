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
@Table ( name ="government_user" )
public class GovernmentUser extends BaseDomain {

	/**
	 * 唯一标识id
	 */
   	@Column(name = "government_id" )
	private String governmentId;

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
   	@Column(name = "account_id" )
	private String accountId;

}
