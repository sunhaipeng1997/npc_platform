package com.cdkhd.npc.entity;

import java.util.Date;
import java.util.Set;

public class Token {
	//签发时间
	private Date signAt;

	//过期时间
	private Date expireAt;

	//用户名
	private String username;

	//用户角色
	private Set<String> roles;

	public Date getSignAt() {
		return signAt;
	}

	public void setSignAt(Date signAt) {
		this.signAt = signAt;
	}

	public Date getExpireAt() {
		return expireAt;
	}

	public void setExpireAt(Date expireAt) {
		this.expireAt = expireAt;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
}
