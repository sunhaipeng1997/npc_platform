package com.cdkhd.npc.entity;

import javax.persistence.*;

import com.cdkhd.npc.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@Table (name ="permission")
public class Permission extends BaseDomain {

	//权限关键字
   	@Column
	private String keyword;

   	//权限状态
   	@Column
	private Byte status = StatusEnum.ENABLED.getValue();

   	//权限名称
   	@Column
	private String name;

   	//关联菜单
   	@OneToMany(mappedBy = "permission", targetEntity = Menu.class, fetch = FetchType.LAZY)
    private Set<Menu> menus;
}
