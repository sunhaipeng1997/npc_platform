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
@Table ( name ="npc_member_role" )
public class NpcMemberRole  implements Serializable {

	private static final long serialVersionUID =  4832698148038476931L;

   	@Column(name = "npc_member_id" )
	private Long npcMemberId;

   	@Column(name = "role_id" )
	private Long roleId;

}
