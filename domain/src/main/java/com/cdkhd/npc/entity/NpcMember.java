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
@Table ( name ="npc_member" )
public class NpcMember extends BaseDomain {

	/**
	 * 唯一标识id
	 */
   	@Column(name = "session_mid_id" )
	private String sessionMidId;

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

   	@Column(name = "email" )
	private String email;

   	@Column(name = "address" )
	private String address;

	/**
	 * yyyy-MM-dd
	 */
   	@Column(name = "birthday" )
	private Date birthday;

	/**
	 * 1、男
            2、女
	 */
   	@Column(name = "gender" )
	private Integer gender;

	/**
	 * 1、人大代表
            2、人大委员会成员

	 */
   	@Column(name = "type" )
	private Integer type;

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

   	@Column(name = "avatar" )
	private String avatar;

   	@Column(name = "introduction" )
	private String introduction;

   	@Column(name = "comment" )
	private String comment;

   	@Column(name = "nation" )
	private String nation;

	/**
	 * 等级
            1、镇代表
            2、区代表
	 */
   	@Column(name = "level" )
	private Integer level;

   	@Column(name = "can_opinion" )
	private Integer canOpinion;

   	@Column(name = "is_del" )
	private Integer isDel;

   	@Column(name = "education" )
	private String education;

   	@Column(name = "jobs" )
	private String jobs;

   	@Column(name = "political" )
	private String political;

   	@Column(name = "joining_time" )
	private Date joiningTime;

   	@Column(name = "area_id" )
	private String areaId;

	/**
	 * 唯一标识id
	 */
   	@Column(name = "town_id" )
	private String townId;

   	@Column(name = "group_id" )
	private String groupId;

	/**
	 * 是否特殊人员
            1、是
            2、否
	 */
   	@Column(name = "special" )
	private Integer special;

}
