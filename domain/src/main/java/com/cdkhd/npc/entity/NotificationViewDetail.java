package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "t_notification_view_detail")
public class NotificationViewDetail extends BaseDomain {

    //所属通知
    @ManyToOne(targetEntity = Notification.class, fetch = FetchType.LAZY)
    private Notification notification;

    //接收代表
    @ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    private NpcMember receiver;

    //代表是否已读该通知
    @Column(name = "read" )
    private Boolean read;
}
