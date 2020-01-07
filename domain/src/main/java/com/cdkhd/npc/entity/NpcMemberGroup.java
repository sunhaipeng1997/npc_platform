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
@Table ( name ="npc_member_group" )
public class NpcMemberGroup extends BaseDomain {

   	@Column(name = "description" )
	private String description;

   	@Column(name = "name" )
	private String name;

    @OneToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
	private NpcMember leaderId;

    @Column(name = "level" )
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
