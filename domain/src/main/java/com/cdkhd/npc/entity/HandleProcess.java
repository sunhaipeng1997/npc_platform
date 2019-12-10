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
@Table ( name ="handle_process" )
public class HandleProcess extends BaseDomain {


   	@Column(name = "description" )
	private String description;

   	@Column(name = "attachement_mid_id" )
	private String attachementMidId;

}
