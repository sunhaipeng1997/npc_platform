package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="menu" )
public class Menu extends BaseDomain {

   	@Column(name = "name")
	private String name;

   	@Column(name = "url")
	private String url;

    @Column(name = "icon")
    private String icon;

    //1 小程序菜单  2 后台系统菜单
    @Column(name = "type")
    private Byte type;

    @Column(name = "keyword")
    private String keyword;

    @ManyToOne(targetEntity = Menu.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent", referencedColumnName = "id")
    private Menu parent;

   	@Column(name = "enabled")
	private Byte enabled = StatusEnum.ENABLED.getValue();

    //关联系统
    @ManyToOne(targetEntity = Permission.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "permission", referencedColumnName = "id")
    private Permission permission;

    //关联系统
    @ManyToOne(targetEntity = Systems.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "system", referencedColumnName = "id")
    private Systems systems;
}
