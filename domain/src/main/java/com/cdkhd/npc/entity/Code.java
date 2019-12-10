package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@Table ( name ="code" )
public class Code extends BaseDomain {

   	@Column(name = "create_at" )
	private Date createAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "code" )
	private String code;

   	@Column(name = "mobile" )
	private String mobile;

   	@Column(name = "valid" )
	private Boolean valid;

}
