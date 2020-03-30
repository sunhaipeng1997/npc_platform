package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NotificationViewDetail;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Getter
@Setter
public class NotificationMobileReceivedPageVo {

    private String uid;

    //通知标题
    private String title;

    //时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;

    //发布时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date publishAt;

    private Integer status;

    private Boolean read;

    public static NotificationMobileReceivedPageVo convert(NotificationViewDetail notificationViewDetail) {
        NotificationMobileReceivedPageVo vo = new NotificationMobileReceivedPageVo();

        // 拷贝一些基本属性
        BeanUtils.copyProperties(notificationViewDetail, vo);

        // 需要特殊处理的属性
        vo.setUid(notificationViewDetail.getNotification().getUid());
        vo.setTitle(notificationViewDetail.getNotification().getTitle());
        vo.setCreateTime(notificationViewDetail.getNotification().getCreateTime());
        vo.setPublishAt(notificationViewDetail.getNotification().getPublishAt());
        vo.setStatus(notificationViewDetail.getNotification().getStatus());

        //该代表是否有查看该通知
        vo.setRead(notificationViewDetail.getIsRead());
        return vo;
    }
}
