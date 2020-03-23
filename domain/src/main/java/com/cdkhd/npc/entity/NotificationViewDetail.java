package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "t_notification_detail")
public class NotificationViewDetail extends BaseDomain {

    //所属通知
    @ManyToOne(targetEntity = Notification.class, fetch = FetchType.LAZY)
    private Notification notification;

    //接收代表
    @ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    private NpcMember receiver;

    //代表查看状态
    private int myView;
}
