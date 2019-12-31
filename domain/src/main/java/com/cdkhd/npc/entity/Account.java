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
	 * 逻辑刪除标识
	 */
	@Column(name = "is_del" )
	private Integer isDel;

	/**
	 * 登录方式 1、账号密码   2、微信小程序
	 */
	@Column(name = "login_way" )
	private Byte loginWay;

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

	//关联的账号角色
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

	//选民关联
	@OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
	private Voter voter;

	//代表关联
	@OneToMany(mappedBy = "account", targetEntity=NpcMember.class, fetch = FetchType.LAZY)
    private Set<NpcMember> npcMembers = new HashSet<>();

	//后台管理员关联
	@OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
	private BackgroundAdmin backgroundAdmin;

	//政府人员关联
	@OneToOne(mappedBy = "account",targetEntity=GovernmentUser.class, fetch = FetchType.LAZY)
	private GovernmentUser governmentUser;

	//办理单位关联
	@OneToOne(mappedBy = "account",targetEntity=UnitUser.class, fetch = FetchType.LAZY)
	private UnitUser unitUser;

	//上次登录进入的系统
	@OneToOne(targetEntity=Systems.class, fetch = FetchType.LAZY)
	private Systems systems;

    //用户偏好设置
    @OneToOne(targetEntity=UserSetting.class, fetch = FetchType.LAZY)
    private UserSetting userSetting;

}
