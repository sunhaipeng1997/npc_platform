package com.cdkhd.npc.entity;

import javax.persistence.*;
import java.io.Serializable;

import com.cdkhd.npc.enums.StatusEnum;
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
@Table ( name ="unit_user" )
public class UnitUser extends BaseDomain {

	//工作人员姓名
	@Column(nullable = false,name = "name")
	private String name;

	//工作人员性别
	@Column(name = "gender")
	private Byte gender;

	//联系电话
	@Column(name = "mobile")
	private String mobile;

	//状态  1、正常 2、锁定
	@Column(name = "status")
	private Byte status = StatusEnum.ENABLED.getValue();

	// 备注情况
	@Column(name = "comment")
	private String comment;

	@Column(nullable = false, name = "avatar")
	private String avatar;

	//办理单位基本信息
	@ManyToOne(targetEntity = Unit.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "unit", referencedColumnName = "id")
	private Unit unit;

	/**
	 * 账号表
	 */
	@OneToOne(targetEntity=Account.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "account", referencedColumnName = "id")
	private Account account;

	@Column(name = "is_del" )
	private Boolean isDel = false;
}
