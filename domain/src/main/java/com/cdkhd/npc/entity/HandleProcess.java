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
@Table ( name ="handle_process" )
public class HandleProcess extends BaseDomain {


   	@Column(name = "description" )
	private String description;

   	@Column(name = "attachment_mid_id" )
	private String attachmentMidId;

	// 办理单位办理记录
	@ManyToOne(targetEntity = UnitSuggestion.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "unitSuggestion", referencedColumnName = "id")
	private UnitSuggestion unitSuggestion;

}
