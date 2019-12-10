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
@Table ( name ="legal" )
public class Legal extends BaseDomain {

   	@Column(name = "create_at" )
	private Date createAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "content" )
	private String content;

   	@Column(name = "cover" )
	private String cover;

   	@Column(name = "法律名称" )
	private String 法律名称;

   	@Column(name = "title" )
	private String title;

   	@Column(name = "type" )
	private Long type;

   	@Column(name = "town_id" )
	private String townId;

   	@Column(name = "level" )
	private Integer level;

}
