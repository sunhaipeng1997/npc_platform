package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="role_permission_mid" )
public class RolePermissionMid  implements Serializable {

	private static final long serialVersionUID =  774897072078271403L;

   	@Column(name = "id" )
	private String id;

   	@Column(name = "role_id" )
	private String roleId;

   	@Column(name = "permission_id" )
	private String permissionId;

}
