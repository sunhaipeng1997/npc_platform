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
@Table ( name ="suggestion" )
public class Suggestion extends BaseDomain {

   	@Column(name = "title" )
	private String title;

   	@Column(name = "business" )
	private String business;

   	@Column(name = "content" )
	private String content;

   	@Column(name = "raiser" )
	private String raiser;

	/**
	 * -1、审核失败
            1、未提交
            2、已提交审核
            3、转交办理单位
            4、办理中
            5、办理完成
            6、办结
	 */
   	@Column(name = "status" )
	private Integer status;

   	@Column(name = "leader" )
	private String leader;

   	@Column(name = "mobile" )
	private String mobile;

   	@Column(name = "raise_time" )
	private Date raiseTime;

   	@Column(name = "audditor" )
	private String audditor;

   	@Column(name = "audit" )
	private Integer audit;

   	@Column(name = "audit_reason" )
	private String auditReason;

   	@Column(name = "audit_time" )
	private Date auditTime;

   	@Column(name = "convey_id" )
	private String conveyId;

   	@Column(name = "convey_time" )
	private Date conveyTime;

   	@Column(name = "accept" )
	private Integer accept;

   	@Column(name = "refusal_reason" )
	private String refusalReason;

   	@Column(name = "refuse_times" )
	private Integer refuseTimes;

   	@Column(name = "unit" )
	private String unit;

   	@Column(name = "accept_time" )
	private Date acceptTime;

   	@Column(name = "deal_times" )
	private Integer dealTimes;

   	@Column(name = "finish_time" )
	private Date finishTime;

   	@Column(name = "accomplish_time" )
	private Date accomplishTime;

   	@Column(name = "urge" )
	private Integer urge;

   	@Column(name = "view" )
	private Integer view;

}
