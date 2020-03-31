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
@Table ( name ="npc_member" )
public class NpcMember extends BaseDomain {

	/**
	 * 届次信息
	 */
	@ManyToMany(targetEntity = Session.class)
	@JoinTable(
			name = "npc_member_session_mid",
			joinColumns = {
					@JoinColumn(name = "npc_member_id", referencedColumnName = "id", nullable = false)
			},
			inverseJoinColumns = {
					@JoinColumn(name = "session_id", referencedColumnName = "id", nullable = false)
			}
	)
	private Set<Session> sessions = new HashSet<>();

	/**
	 * 代表角色
	 */
	@ManyToMany(targetEntity = NpcMemberRole.class)
	@JoinTable(
			name = "npc_member_role_mid",
			joinColumns = {
					@JoinColumn(name = "npc_member_id", referencedColumnName = "id", nullable = false)
			},
			inverseJoinColumns = {
					@JoinColumn(name = "npc_member_role_id", referencedColumnName = "id", nullable = false)
			}
	)
	private Set<NpcMemberRole> npcMemberRoles = new HashSet<>();

	/**
	 * 1、正常
	 * 2、锁定
	 */
	@Column(name = "status" )
	private Byte status = 1;

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
	 * 邮箱
	 */
	@Column(name = "email" )
	private String email;

	/**
	 * 地址
	 */
	@Column(name = "address" )
	private String address;

	/**
	 * yyyy-MM-dd
	 */
	@Column(name = "birthday" )
	@Temporal(TemporalType.DATE)
	private Date birthday;

	/**
	 * 1、男
	 * 2、女
	 */
	@Column(name = "gender" )
	private Byte gender;

	/**
	 * 1、人大代表
	 * 2、人大主席
	 * 3、特殊人员
	 */
	@Column(name = "type" )
	private String type;

	@Column(name = "type_name" )
	private String typeName;

	/**
	 * 代表证号
	 */
	@Column(name = "code" )
	private String code;

	/**
	 * 代表身份证号
	 */
	@Column(name = "idcard" )
	private String idcard;

	/**
	 * 账号表id
	 */
	@ManyToOne(targetEntity=Account.class, fetch = FetchType.LAZY)
	private Account account;

	//头像
	@Column(name = "avatar" )
	private String avatar;

	//简介
	@Column(name = "introduction" )
	private String introduction;

	//备注
	@Column(name = "comment" )
	private String comment;

	//民族
	@Column(name = "nation" )
	private String nation;

	//是否能被提意见
	@Column(name = "can_opinion")
	private Byte canOpinion = 1;

	//是否删除
	@Column(name = "is_del" )
	private Boolean isDel = false;

	//教育经历
	@Column(name = "education" )
	private String education;

	//现任职务
	@Column(name = "jobs" )
	private String jobs;

	//政治面貌
	@Column(name = "political" )
	private String political;

	//是否只能作为特殊职能 1，是  0，不是
	private Byte special = 0;

	/**
	 *   等级
	 *   1、镇代表
	 *   2、区代表
	 */
	@Column(name = "level" )
	private Byte level;

	//关联区
	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "area", referencedColumnName = "id")
	private Area area;

	//关联镇
	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "town", referencedColumnName = "id")
	private Town town;

	//关联小组
	@ManyToOne(targetEntity = NpcMemberGroup.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "npc_member_group", referencedColumnName = "id")
	private NpcMemberGroup npcMemberGroup;

	@ManyToMany(targetEntity = Notification.class)
	private Set<Notification> receivedNotifications = new HashSet<>();

}
