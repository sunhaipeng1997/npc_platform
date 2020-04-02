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
@Table ( name ="suggestion_reply" )
public class SuggestionReply extends BaseDomain {
	//回复内容
   	@Column(name = "reply" )
	private String reply;

	//代表查看回复状态
   	@Column(name = "view" )
	private int view;

	// 关联的建议
	@ManyToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "suggestion", referencedColumnName = "id")
	private Suggestion suggestion;

	//回复的代表
	@ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "replyer", referencedColumnName = "id")
	private NpcMember replyer;

}
