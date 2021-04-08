package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table( name ="background_admin" )
public class BackgroundAdmin extends BaseDomain {
    /**
     *   等级，县/区后台管理员 or 镇后台管理员
     *   见com.cdkhd.npc.enums.LevelEnum
     */
    @Column(name = "level")
    private Byte level;

    //关联区
    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    //关联镇
    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    //关联的账号信息
    @OneToOne
    private Account account;
}
