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
@Table ( name ="permission_menu_mid" )
public class PermissionMenuMid  implements Serializable {

	private static final long serialVersionUID =  6274922817190103006L;

   	@Column(name = "id" )
	private String id;

   	@Column(name = "permission_id" )
	private String permissionId;

   	@Column(name = "menu_id" )
	private String menuId;

}
