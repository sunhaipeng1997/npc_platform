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
@Table ( name ="news_type" )
public class NewsType extends BaseDomain {

   	@Column(name = "creat_at" )
	private Date creatAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "name" )
	private String name;

   	@Column(name = "area" )
	private Integer area;

   	@Column(name = "town_id" )
	private String townId;

   	@Column(name = "remark" )
	private String remark;

}
