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

    /**
     * 1、男
     * 2、女
     */
    @Column(name = "gender" )
    private Byte gender;

    /**
     * 年龄
     */
    @Column(name = "age" )
    private Integer age;

    //关联区
    @ManyToOne(targetEntity = Area.class)//, fetch = FetchType.LAZY)
    @JoinColumn(name = "area", referencedColumnName = "id")
    private Area area;

    //关联镇
    @ManyToOne(targetEntity = Town.class)//, fetch = FetchType.LAZY)
    @JoinColumn(name = "town", referencedColumnName = "id")
    private Town town;

    //关联村
    @ManyToOne(targetEntity = Village.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "village", referencedColumnName = "id")
    private Village village;

    //修改个人信息的次数
    private Integer updateInfo = 0;

    //关联的账号信息
    @OneToOne(targetEntity = Account.class, fetch = FetchType.LAZY,cascade  = CascadeType.ALL)
    @JoinColumn(name = "account", referencedColumnName = "id")
    private Account account;
}
