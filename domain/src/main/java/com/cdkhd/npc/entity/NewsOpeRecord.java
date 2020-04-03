package com.cdkhd.npc.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@Getter
@Setter
@Entity
@Table(name = "news_ope_record")
public class NewsOpeRecord  extends BaseDomain{

    //所属新闻
    @ManyToOne(targetEntity = News.class, fetch = FetchType.LAZY,cascade = CascadeType.ALL)
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
//    @OneToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY,cascade = CascadeType.ALL)
//    @JoinColumn(name = "operator_npcMember", referencedColumnName = "id")
    @Column(name = "operator" )
    private String operator;

    //执行的操作
    @Column(name = "action" )
    private String action;

    //操作时间
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "op_ime")
    private Date opTime = new Date();


}
