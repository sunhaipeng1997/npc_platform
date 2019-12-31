package com.cdkhd.npc.entity;

import javax.persistence.*;
import java.io.Serializable;

import com.cdkhd.npc.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;
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
@Table ( name ="account_role" )
public class AccountRole extends BaseDomain {

   	@Column
	private String keyword;

   	//是否可用
   	@Column
	private Byte status = StatusEnum.ENABLED.getValue();

	//账号身份角色信息  1、人大  2、选民  3、政府  4、办理单位  5、后台管理员
	@Column
	private String name;

	@ManyToMany
	@JoinTable(
			name = "account_role_permission_mid",
			joinColumns = {
					@JoinColumn(name = "account_role_id", referencedColumnName = "id")
			},
			inverseJoinColumns = {
					@JoinColumn(name = "permission_id", referencedColumnName = "id")
			}
	)
	private Set<Permission> permissions;
}
