package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "suggestion_setting")
public class SuggestionSetting extends BaseDomain {

    //办理期限（单位：天）
    @Column(name = "expect_date", nullable = false)
    private Integer expectDate = 90;

    //临期提醒（单位：天）
    @Column(name = "deadline", nullable = false)
    private Integer deadline = 15;

    //催办频率（单位：天）
    @Column(name = "urge_fre", nullable = false)
    private Integer urgeFre = 3;

    @Column(name = "update_user")
    private String updateUser;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "level" )
    private Byte level;

    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

}
