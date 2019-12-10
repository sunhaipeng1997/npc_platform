package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
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
@Table ( name ="account_up" )
public class AccountUP extends BaseDomain {

	/**
	 * 账号
	 */
   	@Column(name = "user_name" )
	private String userName;

	/**
	 * 密码
	 */
   	@Column(name = "password" )
	private String password;

	/**
	 * 1、代表
		2、人大
		3、政府、目督办
		4、承办单位
		5、选民
	 */
   	@Column(name = "type" )
	private Integer type;

	/**
	 * 1、正常
            2、锁定
	 */
   	@Column(name = "status" )
	private Integer status;

	/**
	 * 登录次数
	 */
   	@Column(name = "login_times" )
	private Integer loginTimes;

	/**
	 * 登录时间
	 */
   	@Column(name = "login_time" )
	private Date loginTime;

	/**
	 * 上次登录时间
	 */
   	@Column(name = "last_login_time" )
	private Date lastLoginTime;

	/**
	 * 创建时间
	 */
   	@Column(name = "create_time" )
	private Date createTime;

	/**
	 * 修改时间
	 */
   	@Column(name = "update_time" )
	private Date updateTime;

	/**
	 * 创建人
	 */
   	@Column(name = "create_user" )
	private String createUser;

	/**
	 * 修改人
	 */
   	@Column(name = "update_user" )
	private String updateUser;

	/**
	 * 逻辑刪除标识
	 */
	@Column(name = "is_del" )
	private Integer isDel;

	/**
	 * 手机号
	 */
   	@Column(name = "mobile" )
	private String mobile;


	/**
	 * 真实姓名
	 */
	@Column(name = "realname" )
	private String realname;

	/**
	 * 所属区
	 */
   	@Column(name = "area_id" )
	private Integer areaId;

	/**
	 * 所属镇
	 */
	@Column(name = "town_id" )
	private String townId;

	/**
	 * 所属村
	 */
   	@Column(name = "village_id" )
	private Long villageId;

}
