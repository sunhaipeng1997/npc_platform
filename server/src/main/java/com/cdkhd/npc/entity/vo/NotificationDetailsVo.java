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
 * 后台前端通知详情
 */
@Getter
@Setter
public class NotificationDetailsVo extends BaseVo {
    private String title;

    private String content;

    private String department;

    private Byte type;

    private Set<Attachment> fileList;
//    private List<Map<String,String>> fileList;

    private String reviewerName;

    private Integer status;
    private String statusName;

    //方便后台前端显示通知接受者
    private List<List<String>> receiversUid;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishAt;

    public static NotificationDetailsVo convert(Notification notification) {
        NotificationDetailsVo vo = new NotificationDetailsVo();

        BeanUtils.copyProperties(notification, vo);
        vo.setStatusName(NotificationStatusEnum.values()[notification.getStatus()].getName());

        //此审核人是实际审核该通知的人，存储在NpcMember表中
//        因为数据库表的关联还没确定好，通知审核人还没设置
//        vo.setReviewerName(notification.getReviewer().getName());

        //方便前端展示附件列表
        vo.setFileList(notification.getAttachments());

        //方便前端展示级联选择器
        Set<NpcMember> receivers = notification.getReceivers();
        List<List<String>> receiverUidList = new ArrayList<>();
        for(NpcMember npcMember:receivers){
            List<String> list = new ArrayList<>();
            list.add(npcMember.getNpcMemberGroup().getUid());
            list.add(npcMember.getUid());
            receiverUidList.add(list);
        }
        vo.setReceiversUid(receiverUidList);

        return vo;
    }
}
