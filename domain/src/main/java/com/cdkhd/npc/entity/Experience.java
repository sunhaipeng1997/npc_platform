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
@Table ( name ="experience" )
public class Experience extends BaseDomain {

   	@Column(name = "create_at" )
	private Date createAt;

   	@Column(name = "uid" )
	private String uid;

   	@Column(name = "content" )
	private String content;

   	@Column(name = "my_view" )
	private Long myView;

   	@Column(name = "reason" )
	private String reason;

   	@Column(name = "status" )
	private Long status;

   	@Column(name = "title" )
	private String title;

   	@Column(name = "type_id" )
	private Long typeId;

   	@Column(name = "view" )
	private Long view;

   	@Column(name = "work_at" )
	private Date workAt;

   	@Column(name = "account_id" )
	private Long accountId;

   	@Column(name = "member_id" )
	private Long memberId;

   	@Column(name = "level" )
	private Integer level;

   	@Column(name = "town_id" )
	private String townId;

   	@Column(name = "area_id" )
	private String areaId;

}
