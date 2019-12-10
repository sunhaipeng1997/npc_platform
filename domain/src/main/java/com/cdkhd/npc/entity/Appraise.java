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
@Table ( name ="appraise" )
public class Appraise extends BaseDomain {


   	@Column(name = "suggestion_id" )
	private String suggestionId;

   	@Column(name = "result" )
	private Integer result;

   	@Column(name = "attitude" )
	private Integer attitude;

   	@Column(name = "reason" )
	private String reason;

}
