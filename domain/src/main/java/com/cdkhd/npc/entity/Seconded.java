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
@Table ( name ="seconded" )
public class Seconded extends BaseDomain {

	/**
	 * 1、同意
            2、不同意
	 */
   	@Column(name = "addition" )
	private Integer addition;

   	@Column(name = "attachment_id" )
	private String attachmentId;

}
