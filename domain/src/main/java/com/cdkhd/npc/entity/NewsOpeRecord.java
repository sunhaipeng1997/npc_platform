package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Getter
@Setter
@Entity
@Table(name = "t_news_ope_record")
public class NewsOpeRecord  extends BaseDomain{

    //所属新闻
    @ManyToOne(targetEntity = News.class, fetch = FetchType.LAZY)
    private News news;

    //操作之前，新闻的初始状态
    @Column(name = "original_status" )
    private Integer originalStatus;

    //操作之后，新闻的结果状态
    @Column(name = "result_status" )
    private Integer resultStatus;

    //操作人的反馈或备注,是HTML富文本，将其设置为字符串大对象，并懒加载
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "feedback" ,nullable = true)
    private String feedback;

    //操作人
    @OneToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_npcMember", referencedColumnName = "id")
    private NpcMember operator;


}
