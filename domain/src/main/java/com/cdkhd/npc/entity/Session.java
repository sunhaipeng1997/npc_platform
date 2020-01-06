package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
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
@Table ( name ="session" )
public class Session extends BaseDomain {

	/**
	 * 届期名称
	 */
   	@Column(name = "name" )
	private String name;

	/**
	 * 届期开始时间
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "start_time" )
	private Date startTime;

	/**
	 * 届期结束时间
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "end_time" )
	private Date endTime;

   	//备注
   	@Column(name = "remark" )
	private String remark;

   	//届期的等级，区的届期/镇的届期
	@Column
   	private Byte level;

	//关联区
	@ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "area", referencedColumnName = "id")
	private Area area;

	//关联镇
	@ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "town", referencedColumnName = "id")
	private Town town;
}
