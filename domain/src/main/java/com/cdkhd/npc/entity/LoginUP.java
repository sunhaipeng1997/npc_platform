package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
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
	 * 账号表id
	 */
	@OneToOne(targetEntity=Account.class, fetch = FetchType.LAZY)
	private Account account;


}
