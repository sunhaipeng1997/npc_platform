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
@Table ( name ="opinion_reply" )
public class OpinionReply extends BaseDomain {


    /**
     * 回复内容
     */
   	@Column(name = "reply" )
	private String reply;

    /**
     * 是否查看
     */
   	@Column(name = "view" )
	private Boolean view = false;

    /**
     * 回复的意见
     */
    @ManyToOne(targetEntity = Opinion.class, fetch = FetchType.LAZY)
    private Opinion opinion;

}
