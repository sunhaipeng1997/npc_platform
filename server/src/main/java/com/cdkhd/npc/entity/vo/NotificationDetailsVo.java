package com.cdkhd.npc.entity.vo;


import com.cdkhd.npc.entity.Attachment;
import com.cdkhd.npc.entity.Notification;
import com.cdkhd.npc.enums.NewsStatusEnum;
import com.cdkhd.npc.enums.NotificationStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class NotificationDetailsVo extends BaseVo {
    private String title;

    private String content;

    private Set<Attachment> attachments;

    private String reviewerName;

    private String statusName;

    private String feedback;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishAt;

    public static NotificationDetailsVo convert(Notification notification) {
        NotificationDetailsVo vo = new NotificationDetailsVo();

        BeanUtils.copyProperties(notification, vo);
        vo.setStatusName(NotificationStatusEnum.values()[notification.getStatus()].getName());

        //此审核人是实际审核该通知的人，存储在NpcMember表中
//        因为数据库表的关联还没确定好，通知审核人还没设置
        vo.setReviewerName(notification.getReviewer().getName());

        return vo;
    }
}
