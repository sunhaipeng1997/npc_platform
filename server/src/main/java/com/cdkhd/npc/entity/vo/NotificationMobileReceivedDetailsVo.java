package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Attachment;
import com.cdkhd.npc.entity.Notification;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.enums.NotificationStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/*
 * 移动端接收人的通知详情
 */
@Getter
@Setter
public class NotificationMobileReceivedDetailsVo extends BaseVo {
    private String title;

    private String content;

    private String department;

    private Byte type;

    private Set<Attachment> fileList;
//    private List<Map<String,String>> fileList;

    private Integer status;
    private String statusName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishAt;

    public static NotificationMobileReceivedDetailsVo convert(Notification notification) {
        NotificationMobileReceivedDetailsVo vo = new NotificationMobileReceivedDetailsVo();

        BeanUtils.copyProperties(notification, vo);
        vo.setStatusName(NotificationStatusEnum.values()[notification.getStatus()].getName());

        //附件列表
        vo.setFileList(notification.getAttachments());

        return vo;
    }
}
