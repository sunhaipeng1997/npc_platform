package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Setter
@Getter
@ToString
@Entity
@Table( name ="voter" )
public class Voter extends BaseDomain {
    /**
     * 手机号
     */
    @Column(name = "mobile" )
    private String mobile;

    /**
     * 真实姓名
     */
    @Column(name = "realname" )
    private String realname;

    //关联区
    @ManyToOne(targetEntity = Area.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", referencedColumnName = "id")
    private Area area;

    //关联镇
    @ManyToOne(targetEntity = Town.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "town_id", referencedColumnName = "id")
    private Town town;

    //关联村
    @ManyToOne(targetEntity = Village.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id", referencedColumnName = "id")
    private Village village;

    //修改个人信息的次数
    private Integer updateInfo = 0;

    //关联的账号信息
    @OneToOne(fetch = FetchType.LAZY)
    private Account account;
}
