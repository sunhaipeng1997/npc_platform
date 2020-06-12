package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="appraise" )
public class Appraise extends BaseDomain {

	//办理结果是否满意 评分 1/2/3/4/5
   	@Column(name = "result" )
	private Byte result;

	//办理态度是否满意 评分 1/2/3/4/5
   	@Column(name = "attitude" )
	private Byte attitude;

   	//原因
   	@Column(name = "reason" )
	private String reason;

	//评价人
	@OneToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
	private NpcMember npcMember;

	//对应的建议
	@OneToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "suggestion", referencedColumnName = "id")
	private Suggestion suggestion;

}
