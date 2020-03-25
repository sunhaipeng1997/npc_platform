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
	private Byte enabled;

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
	 * url
	 */
	@Column(name = "keyword")
	private String keyword;

}
