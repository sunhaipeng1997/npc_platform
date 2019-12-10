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
@Table ( name ="account_role_mid" )
public class AccountRoleMid  implements Serializable {

	private static final long serialVersionUID =  8881007872122951819L;

   	@Column(name = "id" )
	private String id;

   	@Column(name = "account_id" )
	private String accountId;

   	@Column(name = "role_id" )
	private String roleId;

}
