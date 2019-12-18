package com.cdkhd.npc.entity;

import javax.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@Table ( name ="account_role" )
public class AccountRole extends BaseDomain {

	//账号身份角色信息  1、代表  2、选民  3、政府  4、办理单位
   	@Column(name = "role_name" )
	private String roleName;

   	@Column(name = "role_code" )
	private String roleCode;

   	//是否可用
   	@Column(name = "enabled" )
	private Boolean enabled;



}
