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
@Table ( name ="suggestion_setting" )
public class SuggestionSetting extends BaseDomain {

	/**
	 * 姓名
	 */
   	@Column(name = "finish_time" )
	private Date finishTime;

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "create_time" )
	private Date createTime;

	/**
	 * 基本信息id
	 */
   	@Column(name = "create_user" )
	private String createUser;

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "update_tTime" )
	private Date updateTTime;

	/**
	 * 基本信息id
	 */
   	@Column(name = "update_user" )
	private String updateUser;

}
