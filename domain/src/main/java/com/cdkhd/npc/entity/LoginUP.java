package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="login_up" )
public class LoginUP extends BaseDomain {

	/**
	 * 账号
	 */
   	@Column(name = "username" )
	private String username;

	/**
	 * 密码
	 */
   	@Column(name = "password" )
	private String password;

	/**
	 * 登录验证手机号
	 */
	@Column
	private String mobile;

	/**
	 * 账号表id
	 */
	@OneToOne//(//targetEntity=Account.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "account")//, referencedColumnName = "id")
	private Account account;

}
