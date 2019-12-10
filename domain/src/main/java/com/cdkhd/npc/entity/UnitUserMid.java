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
@Table ( name ="unit_user_mid" )
public class UnitUserMid  implements Serializable {

	private static final long serialVersionUID =  4218997472677686186L;

	/**
	 * 唯一标识id
	 */
   	@Column(name = "id" )
	private String id;

   	@Column(name = "unit_id" )
	private String unitId;

   	@Column(name = "unit_user_id" )
	private String unitUserId;

}
