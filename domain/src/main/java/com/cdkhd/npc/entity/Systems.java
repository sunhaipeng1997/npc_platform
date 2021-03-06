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
@Entity
@Table ( name ="systems" )
public class Systems extends BaseDomain {

	/**
	 * 系统名称
	 */
   	@Column(name = "name")
	private String name;

	/**
	 * 是否可用
	 */
	@Column(name = "enabled")
	private Boolean enabled;

	/**
	 * 系统图标
	 */
   	@Column(name = "svg")
	private String svg;

    /**
     * 描述
     */
    @Column(name = "description")
    private String description;

    /**
     * url
     */
    @Column(name = "url")
    private String url;

	/**
	 * keyword
	 */
	@Column(name = "keyword")
	private String keyword;

	/**
	 * 小程序系统图标
	 */
	@Column(name = "img_url")
	private String imgUrl;

	/**
	 * 小程序跳转路径
	 */
	@Column(name = "page_path")
	private String pagePath;

	/**
	 * 小程序是否展示
	 */
	@Column(name = "mini_show")
	private Boolean miniShow;

	/**
	 * 排序号
	 */
	@Column(name = "sequence")
	private Integer sequence;



}
