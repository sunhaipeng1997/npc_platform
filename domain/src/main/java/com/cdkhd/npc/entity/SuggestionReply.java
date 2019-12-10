package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
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
@Table ( name ="suggestion_reply" )
public class SuggestionReply extends BaseDomain {


   	@Column(name = "create_at" )
	private Date createAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "reply" )
	private String reply;

   	@Column(name = "view" )
	private Long view;

   	@Column(name = "replyer_id" )
	private Long replyerId;

   	@Column(name = "suggestion_id" )
	private Long suggestionId;

}
