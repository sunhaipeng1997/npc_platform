package com.cdkhd.npc.entity;

import javax.persistence.*;
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
	 * 完成时间
	 */
   	@Column(name = "finish_time" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishTime;

	/**
	 * 基本信息id
	 */
   	@Column(name = "create_user" )
	private String createUser;

	/**
	 * yyyy-MM-dd HH:mm:ss
	 */
   	@Column(name = "update_time" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

	/**
	 * 基本信息id
	 */
   	@Column(name = "update_user" )
	private String updateUser;

}
