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
@Table ( name ="area" )
public class Area extends BaseDomain {

   	@Column(name = "create_time" )
	private Date createTime;

   	@Column(name = "name" )
	private String name;

   	@Column(name = "remark" )
	private String remark;

   	@Column(name = "status" )
	private Integer status;

   	@Column(name = "update_time" )
	private Date updateTime;

   	@Column(name = "update_user" )
	private String updateUser;

}
