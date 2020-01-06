package com.cdkhd.npc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
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
@Table ( name ="news" )
public class News extends BaseDomain {

	@Column(name = "creat_at" )
	private Date creatAt;

   	@Column(name = "audit" )
	private Long audit;

   	@Column(name = "content" )
	private String content;

   	@Column(name = "cover" )
	private String cover;

   	@Column(name = "publish_at" )
	private Date publishAt;

   	@Column(name = "published" )
	private Boolean published;

   	@Column(name = "reason" )
	private String reason;

   	@Column(name = "show_way" )
	private Integer showWay;

   	@Column(name = "title" )
	private String title;

   	@Column(name = "seconded" )
	private String seconded;

   	@Column(name = "type_id" )
	private Long typeId;

   	@Column(name = "view" )
	private Long view;

   	@Column(name = "account_id" )
	private Long accountId;

   	@Column(name = "area" )
	private Integer area;

   	@Column(name = "town_id" )
	private String townId;

	@ManyToOne(targetEntity = NewsType.class)
   	private NewsType newsType;

   	@Column(name = "read_times" )
	private Long readTimes;

}
