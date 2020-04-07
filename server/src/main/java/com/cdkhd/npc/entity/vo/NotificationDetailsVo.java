package com.cdkhd.npc.entity.vo;


import com.cdkhd.npc.entity.Attachment;
import com.cdkhd.npc.entity.Notification;
import com.cdkhd.npc.entity.NotificationOpeRecord;
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

//    private Set<Attachment> fileList;
    private List<AttachmentVo> fileList = new ArrayList<>();

    private String reviewerName;

    private Integer status;
    private String statusName;

    //方便后台前端显示通知接受者
    private List<List<String>> receiversUid = new ArrayList<>();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishAt;

    private List<NotificationOpeRecordVo> opeRecordList= new ArrayList<>();

    public static NotificationDetailsVo convert(Notification notification) {
        NotificationDetailsVo vo = new NotificationDetailsVo();

        BeanUtils.copyProperties(notification, vo);
        vo.setStatusName(NotificationStatusEnum.values()[notification.getStatus()].getName());

        Set<Attachment> attachments = notification.getAttachments();
        if(!attachments.isEmpty()){
            for (Attachment attachment:attachments){
                vo.getFileList().add(AttachmentVo.convert(attachment));
            }
        }

        //方便前端展示级联选择器
        Set<NpcMember> receivers = notification.getReceivers();
        if(!receivers.isEmpty()) {
            for (NpcMember npcMember : receivers) {
                List<String> list = new ArrayList<>();
                if(npcMember.getNpcMemberGroup() != null) {
                    list.add(npcMember.getNpcMemberGroup().getUid());
                }else{
                    list.add(" ");
                }
                list.add(npcMember.getUid());
                vo.getReceiversUid().add(list);
            }
        }

        //将操作记录一并返回
        List<NotificationOpeRecord> opeRecords = notification.getOpeRecords();
        if(!opeRecords.isEmpty()) {
            for (NotificationOpeRecord opeRecord : opeRecords) {
                NotificationOpeRecordVo opeRecordVo = NotificationOpeRecordVo.convert(opeRecord);
                vo.getOpeRecordList().add(opeRecordVo);
            }
        }

        return vo;
    }
}
