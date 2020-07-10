package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "urge")
public class Urge extends BaseDomain {

    //催办来源 1、代表催办 2、联工委催办 3，政府催办
    @Column(nullable = false)
    private Byte type;

    //催办分数 代表催一次 1分 ，联工委催一次 2分 政府催一次 4分，以总分排序提醒进行名
    private Integer score;

    //催办账号  因为联工委没有专门的表，没法表示，所以这里记录账号
    @ManyToOne(targetEntity = Account.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account", referencedColumnName = "id")
    private Account account;

    //对应的建议
    @ManyToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion", referencedColumnName = "id")
    private Suggestion suggestion;

}

