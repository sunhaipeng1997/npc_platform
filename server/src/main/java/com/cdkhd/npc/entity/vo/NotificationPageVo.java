package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Notification;
import com.cdkhd.npc.enums.NotificationStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class NotificationPageVo  {
    @DateTimeFormat(pattern = "MM-dd")
    @JsonFormat(pattern = "MM-dd", timezone = "GMT+8")
    private Date createTime;

    private String uid;
    private String title;

    private String department;

    private Integer status;
    private String statusName;

    //审核人员查看状态
    private Boolean view;

    private Byte type;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishAt;

    public static NotificationPageVo convert(Notification notification) {
        NotificationPageVo vo = new NotificationPageVo();

        BeanUtils.copyProperties(notification, vo);
        vo.setStatusName(NotificationStatusEnum.values()[notification.getStatus()].getName());

        return vo;
    }
}
