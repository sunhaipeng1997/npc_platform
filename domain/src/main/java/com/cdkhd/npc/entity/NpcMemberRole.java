package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
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
@Table ( name ="npc_member_role" )
public class NpcMemberRole extends BaseDomain {

   	@Column
	private String keyword;

   	//是否可用
   	@Column
	private Byte status = StatusEnum.ENABLED.getValue();

	//代表身份角色细分  1、普通代表  2、人大主席  3、特殊人员
   	@Column
	private String name;

	@ManyToMany
	@JoinTable(
			name = "npc_member_role_permission_mid",
			joinColumns = {
					@JoinColumn(name = "npc_member_role_id", referencedColumnName = "id")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "permission_id", referencedColumnName = "id")
			}
	)
   	private Set<Permission> permissions;
}
