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
@Table ( name ="menu" )
public class Menu extends BaseDomain {

   	@Column(name = "name" )
	private String name;

   	@Column(name = "url" )
	private String url;

   	@Column(name = "parent_id" )
	private String parentId;

   	@Column(name = "status" )
	private Integer status;

   	@Column(name = "create_time" )
	private Date createTime;

}
