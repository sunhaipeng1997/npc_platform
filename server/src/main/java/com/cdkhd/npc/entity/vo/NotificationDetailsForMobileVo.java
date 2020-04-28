package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.NotificationStatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

@Getter
@Setter
public class NotificationDetailsForMobileVo extends BaseVo {
    private String title;

    private String content;

    private String department;

    private Byte type;

//    private Set<Attachment> fileList;
    private List<AttachmentVo> fileList = new ArrayList<>();

    private String reviewerName;

    private Integer status;
    private String statusName;

    //方便移动端端显示通知接受者
    private List<List<String>> receiversUid = new ArrayList<>();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date publishAt;

    //操作记录
    private List<NotificationOpeRecordVo> opeRecordList= new ArrayList<>();

    public static NotificationDetailsForMobileVo convert(Notification notification) {
        NotificationDetailsForMobileVo vo = new NotificationDetailsForMobileVo();

        BeanUtils.copyProperties(notification, vo);
        vo.setStatusName(NotificationStatusEnum.values()[notification.getStatus()].getName());

        //方便前端展示附件列表
        Set<Attachment> attachments = notification.getAttachments();
        if(!attachments.isEmpty()){
            for (Attachment attachment:attachments){
                vo.getFileList().add(AttachmentVo.convert(attachment));
            }
        }

        //方便移动端展示级联选择器
//        Set<NpcMember> receivers = notification.getReceivers();

        //为了旧系统迁移过来的数据兼容，改为从viewDetail中获取接收人，免去一张中间表,但是这样效率稍微低一些，要查询两次数据库
        Set<NotificationViewDetail> receiversViewDetail = notification.getReceiversViewDetails();
        if(!receiversViewDetail.isEmpty()) {
            for(NotificationViewDetail viewDetail:receiversViewDetail){
                NpcMember receiver = viewDetail.getReceiver();
                List<String> list = new ArrayList<>();
                if(notification.getLevel().equals(LevelEnum.AREA.getValue())){
                    if(receiver.getTown() != null) {
                        list.add(receiver.getTown().getUid());
                    }else{
                        list.add(" ");
                    }
                }else {
                    if(receiver.getNpcMemberGroup() != null) {
                        list.add(receiver.getNpcMemberGroup().getUid());
                    }else{
                        list.add(" ");
                    }
                }

                list.add(receiver.getUid());
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
