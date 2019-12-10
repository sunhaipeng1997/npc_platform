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
@Table ( name ="npc_member_session_mid" )
public class NpcMemberSessionMid extends BaseDomain {


   	@Column(name = "session_id" )
	private String sessionId;

   	@Column(name = "npc_member_id" )
	private String npcMemberId;

}
