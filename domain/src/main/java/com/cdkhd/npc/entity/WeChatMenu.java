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
@Entity
@Table ( name ="wechat_menu" )
public class WeChatMenu extends BaseDomain {

	/**
	 * appid
	 */
   	@Column(name = "appid" )
	private String appid;

	/**
	 * keyword
	 */
   	@Column(name = "keyword" )
	private String keyword;

	/**
	 * media_id
	 */
   	@Column(name = "media_id" )
	private String mediaId;

	/**
	 * 菜单名称
	 */
   	@Column(name = "name" )
	private String name;

	/**
	 * 页面路径
	 */
   	@Column(name = "pagepath" )
	private String pagepath;

	/**
	 * 菜单类型
	 */
   	@Column(name = "type" )
	private String type;

	/**
	 * unique_key
	 */
   	@Column(name = "unique_key" )
	private String uniqueKey;

	/**
	 * 菜单路径
	 */
   	@Column(name = "url" )
	private String url;

	/**
	 * 父级id
	 */
   	@Column(name = "parent_id" )
	private String parentId;

}
