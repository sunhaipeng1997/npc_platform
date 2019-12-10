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
@Table ( name ="suggestion_attachment_mid" )
public class SuggestionAttachmentMid extends BaseDomain {

   	@Column(name = "host_id" )
	private String hostId;

	/**
	 * 1、建议附件
            2、办理过程附件
            3、办理结果的附件
	 */
   	@Column(name = "type" )
	private Integer type;

}
