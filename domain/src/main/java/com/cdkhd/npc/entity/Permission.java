package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cdkhd.npc.enums.StatusEnum;
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
   	@OneToOne
   	Menu menu;
}
