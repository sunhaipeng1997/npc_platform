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
@Table ( name ="notification" )
public class Notification extends BaseDomain {

   	@Column(name = "create_at" )
	private Date createAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "audit" )
	private Long audit;

   	@Column(name = "content" )
	private String content;

   	@Column(name = "publish_at" )
	private Date publishAt;

   	@Column(name = "published" )
	private Boolean published;

   	@Column(name = "reason" )
	private String reason;

   	@Column(name = "title" )
	private String title;

   	@Column(name = "view" )
	private Long view;

   	@Column(name = "account_id" )
	private Long accountId;

   	@Column(name = "area_id" )
	private Integer areaId;

   	@Column(name = "town_id" )
	private String townId;

	/**
	 * 通知等级
            1、镇上
            2、区上
	 */
   	@Column(name = "level" )
	private Byte level;

}
