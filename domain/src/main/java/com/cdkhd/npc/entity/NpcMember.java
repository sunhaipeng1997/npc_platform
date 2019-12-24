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
	private Date birthday;

	/**
	 * 1、男
	 * 2、女
	 */
   	@Column(name = "gender" )
	private Integer gender;

	/**
	 * 1、人大代表
	 * 2、人大委员会成员
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
	@ManyToOne(targetEntity=Account.class, fetch = FetchType.LAZY)
	private Account account;


   	@Column(name = "avatar" )
	private String avatar;

   	@Column(name = "introduction" )
	private String introduction;

   	@Column(name = "comment" )
	private String comment;

   	@Column(name = "nation" )
	private String nation;

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


    /**
     *   等级
     *   1、镇代表
     *   2、区代表
     */
    @Column(name = "level" )
    private Integer level;

    //关联区
    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    //关联镇
    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    @ManyToOne(targetEntity = NpcMemberGroup.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "group", referencedColumnName = "id")
	private NpcMemberGroup group;

	/**
	 * 是否特殊人员
	 * 1、是
	 * 2、否
	 */
   	@Column(name = "special" )
	private Integer special;

}
