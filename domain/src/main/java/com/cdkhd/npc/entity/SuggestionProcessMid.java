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
@Table ( name ="suggestion_process_mid" )
public class SuggestionProcessMid extends BaseDomain {

   	@Column(name = "suggestion_id" )
	private String suggestionId;

   	@Column(name = "process_id" )
	private String processId;

}
