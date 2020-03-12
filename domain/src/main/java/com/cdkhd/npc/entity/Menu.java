package com.cdkhd.npc.entity;

import javax.persistence.*;
import java.io.Serializable;

import com.cdkhd.npc.enums.StatusEnum;
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
@Table ( name ="menu" )
public class Menu extends BaseDomain {

   	@Column(name = "name")
	private String name;

   	@Column(name = "url")
	private String url;

   	@Column(name = "parent_id")
	private String parentId;

   	@Column(name = "enabled")
	private Byte enabled = StatusEnum.ENABLED.getValue();

    //关联系统
    @ManyToOne(targetEntity = Permission.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "permission", referencedColumnName = "id")
    private Permission permission;

    //关联系统
    @ManyToOne(targetEntity = Systems.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id", referencedColumnName = "id")
    private Systems systems;
}
