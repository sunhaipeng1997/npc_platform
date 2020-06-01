package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
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
@Table ( name ="seconded" )
public class Seconded extends BaseDomain {

	/**
	 *  1、同意
	 *	2、不同意
	 */
   	@Column(name = "addition" )
	private Byte addition;

   	@Column(name = "attachment_id" )
	private String attachmentId;

    @Column(name = "view" )
    private Boolean view = false;

	//附议的时间
	@Column(name = "seconded_time")
	private Date secondedTime = new Date();

	//对应的建议
   	@ManyToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
   	private Suggestion suggestion;

   	//提出附议的代表
	@ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
   	private NpcMember npcMember;

}
