package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="npc_member_role" )
public class NpcMemberRole extends BaseDomain {

	//代表身份角色细分  1、普通代表  2、人大主席  3、特殊人员
   	@Column(name = "role_name" )
	private String roleName;

   	@Column(name = "role_code" )
	private String roleCode;

   	//是否可用
   	@Column(name = "enabled" )
	private Boolean enabled;



}
