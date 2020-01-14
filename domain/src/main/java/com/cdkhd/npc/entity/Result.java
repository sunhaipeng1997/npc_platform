package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="result" )
public class Result extends BaseDomain {

   	@Column(name = "attachment_mid_id" )
	private String attachmentMidId;

   	@Column(name = "suggestion_id" )
	private String suggestionId;

   	@Column(name = "result" )
	private String result;

   	@Column(name = "accepted" )
	private Integer accepted;

   	@Column(name = "reason" )
	private String reason;

}
