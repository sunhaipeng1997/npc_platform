package com.cdkhd.npc.entity;

import com.cdkhd.npc.enums.NotificationStatusEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "notification_ope_record")
public class NotificationOpeRecord  extends BaseDomain{

    //所属通知
    @ManyToOne(targetEntity = Notification.class, fetch = FetchType.LAZY)
    private Notification notification;

    //操作之前，通知的初始状态
    @Column(name = "original_status" )
    private Integer originalStatus;

    //操作之后，通知的结果状态
    @Column(name = "result_status" )
    private Integer resultStatus;

    //操作人的反馈或备注,是HTML富文本，将其设置为字符串大对象，并懒加载
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "feedback" ,nullable = true)
    private String feedback;

    //操作人
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
