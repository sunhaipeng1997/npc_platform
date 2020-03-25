package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class Token {
	//签发时间
	private Date signAt;

	//过期时间
	private Date expireAt;

	//用户名
	private String username;

	private String uid;

	//用户角色
	private Set<String> roles;

	private Account account;
}
