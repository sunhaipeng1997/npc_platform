package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "notification_view_detail")
public class NotificationViewDetail extends BaseDomain {

    //所属通知
    @ManyToOne(targetEntity = Notification.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "notification", referencedColumnName = "id")
    private Notification notification;

    //接收代表
    @ManyToOne(targetEntity = NpcMember.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver", referencedColumnName = "id")
    private NpcMember receiver;

    //代表是否已读该通知
    @Column(name = "is_read" )
    private Boolean isRead;
}
