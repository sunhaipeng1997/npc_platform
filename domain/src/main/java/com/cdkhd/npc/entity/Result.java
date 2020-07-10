package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

/**
 * @Description
 * @Author rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table(name = "result")
public class Result extends BaseDomain {

    //办理结果
    @Column(name = "result")
    private String result;

    //结果图片，存储在UnitImage中
    //建议图片
    @OneToMany(targetEntity = UnitImage.class, mappedBy = "belongToId")
    private Set<UnitImage> resultImages;

    //代表是否接受办理结果
    @Column(name = "accepted")
    private Boolean accepted;

    //代表拒绝的原因
    @Column(name = "reason")
    private String reason;

    //关联的办理单位建议
    @OneToOne(targetEntity = UnitSuggestion.class, fetch = FetchType.LAZY)
    private UnitSuggestion unitSuggestion;

    //对应的建议
    @OneToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion", referencedColumnName = "id")
    private Suggestion suggestion;

}


