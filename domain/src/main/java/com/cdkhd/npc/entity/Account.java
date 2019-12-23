package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="account" )
public class Account extends BaseDomain {

	/**
	 *  1、代表
	 *	2、人大
	 *	3、政府、目督办
	 *	4、承办单位
	 *	5、选民
	 */
   	@Column(name = "indetity" )
	private Byte indetity;

	/**
	 * 1、正常
	 * 2、锁定
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

	//关联区
	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "area_id", referencedColumnName = "id")
	private Area area;

	//关联镇
	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "town_id", referencedColumnName = "id")
	private Town town;


	//关联村
	@ManyToOne(targetEntity = Village.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "village_id", referencedColumnName = "id")
	private Village village;

	//修改个人信息的次数
	private Integer updateInfo = 0;

	/**
	 * 登录方式 1、账号密码   2、微信小程序
	 */
	@Column(name = "login_way" )
	private Byte LoginWay;

	/**
	 * 账号密码信息
	 */
	@OneToOne(mappedBy = "account",targetEntity=LoginUP.class, fetch = FetchType.LAZY)
	private LoginUP loginUP;

	/**
	 * 小程序登录信息
	 */
	@OneToOne(mappedBy = "account",targetEntity=LoginWeChat.class, fetch = FetchType.LAZY)
	private LoginWeChat loginWeChat;

	@ManyToMany(targetEntity = AccountRole.class)
	@JoinTable(
			name = "account_role_mid",
			joinColumns = {
					@JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
			},
			inverseJoinColumns = {
					@JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
			}
	)
	private Set<AccountRole> accountRoles = new HashSet<>();

	//代表关联
	@OneToMany(targetEntity=NpcMember.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account", referencedColumnName = "id")
    private Set<NpcMember> npcMembers = new HashSet<>();

	//政府人员关联
	@OneToOne(mappedBy = "account",targetEntity=GovernmentUser.class, fetch = FetchType.LAZY)
	private GovernmentUser governmentUser;

	//办理单位关联
	@OneToOne(mappedBy = "account",targetEntity=UnitUser.class, fetch = FetchType.LAZY)
	private UnitUser unitUser;

	//上次登录进入的系统
	@OneToOne(targetEntity=Systems.class, fetch = FetchType.LAZY)
	private Systems systems;

}
