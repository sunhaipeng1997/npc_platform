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
@Table ( name ="handle_process" )
public class HandleProcess extends BaseDomain {


   	@Column(name = "description" )
	private String description;

   	@Column(name = "attachment_mid_id" )
	private String attachmentMidId;

}
