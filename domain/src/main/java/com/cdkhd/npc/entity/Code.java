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
@Table ( name ="code" )
public class Code extends BaseDomain {

   	@Column(name = "code" )
	private String code;

   	@Column(name = "mobile" )
	private String mobile;

   	@Column(name = "valid" )
	private Boolean valid;

}
