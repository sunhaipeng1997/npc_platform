package com.cdkhd.npc.repository.base;

import com.cdkhd.npc.entity.NotificationViewDetail;

public interface NotificationViewDetailRepository extends BaseRepository<NotificationViewDetail>{
    NotificationViewDetail findByNotificationUidAndReceiverUid(String notificationUid,String npcMemberUid);
}
