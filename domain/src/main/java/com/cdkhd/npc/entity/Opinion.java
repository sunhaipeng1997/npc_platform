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
@Table ( name ="opinion" )
public class Opinion extends BaseDomain {

   	@Column(name = "create_at" )
	private Date createAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "can_operate" )
	private Boolean canOperate;

   	@Column(name = "content" )
	private String content;

   	@Column(name = "status" )
	private Long status;

   	@Column(name = "view" )
	private Long view;

   	@Column(name = "receiver_id" )
	private Long receiverId;

   	@Column(name = "sender_id" )
	private Long senderId;

   	@Column(name = "area_id" )
	private Integer areaId;

   	@Column(name = "town_id" )
	private String townId;

}
