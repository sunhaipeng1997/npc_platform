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

    //是否接受办理结果
    @Column(name = "accepted")
    private Integer accepted;

    //原因
    @Column(name = "reason")
    private String reason;

    //办理结果 反馈给主办单位
    @OneToOne(targetEntity = UnitSuggestion.class, fetch = FetchType.LAZY)
    private UnitSuggestion unitSuggestion;

    //结果附件
    @OneToMany(targetEntity = Attachment.class, cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    private Set<Attachment> attachments;

    //对应的建议
    @ManyToOne(targetEntity = Suggestion.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "suggestion", referencedColumnName = "id")
    private Suggestion suggestion;

}


