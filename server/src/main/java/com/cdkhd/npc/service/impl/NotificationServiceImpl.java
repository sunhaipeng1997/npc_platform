package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.entity.Attachment;
import com.cdkhd.npc.entity.Notification;
import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.NotificationDetailsVo;
import com.cdkhd.npc.enums.NewsStatusEnum;
import com.cdkhd.npc.enums.NotificationStatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.AttachmentRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.NotificationRepository;
import com.cdkhd.npc.service.NotificationService;
import com.cdkhd.npc.service.PushService;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.util.SysUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private NotificationRepository notificationRepository;
    private AttachmentRepository attachmentRepository;
    private NpcMemberRepository npcMemberRepository;
    private AccountRepository accountRepository;
    private SystemSettingService systemSettingService;
    private PushService pushService;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, AttachmentRepository attachmentRepository, NpcMemberRepository npcMemberRepository, AccountRepository accountRepository, SystemSettingService systemSettingService, PushService pushService) {
        this.notificationRepository = notificationRepository;
        this.attachmentRepository = attachmentRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.accountRepository = accountRepository;
        this.systemSettingService = systemSettingService;
        this.pushService = pushService;
    }

    @Override
    public RespBody uploadAttachment(AttachmentDto dto) {
        RespBody<JSONObject> body = new RespBody<>();

        MultipartFile file = dto.getFile();
        if (file == null) {
            body.setMessage("附件为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Attachment attachment = new Attachment();

        // 保存
        String filename = file.getOriginalFilename();
//        String ext = FilenameUtils.getExtension(org);
        String parentPath = String.format("static/public/notification/%s", attachment.getUid());
        File bgFile = new File(parentPath, filename);
        File parentFile = bgFile.getParentFile();
        if (!parentFile.exists()) {
            boolean mkdirs = parentFile.mkdirs();
            if (!mkdirs) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("系统内部错误");
                return body;
            }
        }

        try (InputStream is = file.getInputStream()) {
            // 拷贝文件
            FileUtils.copyInputStreamToFile(is, bgFile);
        } catch (IOException e) {
            LOGGER.error("通知附件保存失败 {}", e);
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("系统内部错误");
            return body;
        }


        attachment.setUrl(String.format("/public/notification/%s/%s",attachment.getUid(),filename));
        attachmentRepository.save(attachment);

        JSONObject obj = new JSONObject();
        obj.put("attachmentUid",attachment.getUid());
        body.setData(obj);

        return body;
    }

    @Override
    public RespBody add(UserDetailsImpl userDetails, NotificationAddDto dto){
        RespBody body = new RespBody();

        Notification notification = new Notification();
        BeanUtils.copyProperties(dto, notification);
        notification.setArea(userDetails.getArea());
        notification.setTown(userDetails.getTown());
        notification.setLevel(userDetails.getLevel());

        if(!dto.isBillboard() && dto.getReceiversUid().isEmpty()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该通知缺少接收人");
            LOGGER.warn("该通知缺少接收人");
            return body;
        }

        List<String> npcMemberUidlist = JSONObject.parseArray(dto.getReceiversUid().toJSONString(),String.class);
        Set<NpcMember> receivers = new HashSet<>();
        for (String npcMemberUid : npcMemberUidlist){
            NpcMember npcMember = npcMemberRepository.findByUid(npcMemberUid);
            if(npcMember == null){
                body.setStatus(HttpStatus.NOT_FOUND);
                body.setMessage("该通知的接收人不存在");
                LOGGER.warn("uid为 {} 的通知不存在，新增通知失败",npcMemberUid);
                return body;
            }
            receivers.add(npcMember);
        }

        notification.setReceivers(receivers);

        List<String> attachmentUidList = JSONObject.parseArray(dto.getAttachmentsUid().toJSONString(),String.class);
        Set<Attachment> attachments = new HashSet<>();
        for (String attachmentUid : attachmentUidList){
            Attachment attachment = attachmentRepository.findByUid(attachmentUid);
            if(attachment == null){
                body.setStatus(HttpStatus.NOT_FOUND);
                body.setMessage("附件不存在");
                LOGGER.warn("uid为 {} 的附件不存在，新增通知失败",attachmentUid);
                return body;
            }
            attachments.add(attachment);
        }
        notification.setAttachments(attachments);

        //保存数据
        notificationRepository.save(notification);

        body.setMessage("添加通知成功");
        return body;
    }

    @Override
    public RespBody delete(String uid){
        RespBody body = new RespBody();
        Notification notification = notificationRepository.findByUid(uid);
        if(notification == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该通知不存在");
            LOGGER.warn("uid为 {} 的通知不存在，删除通知失败",uid);
            return body;
        }

        //如果通知在审核中，则不允许删除
        if(notification.getStatus() == NotificationStatusEnum.UNDER_REVIEW.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该通知在审核中，不能删除");
            LOGGER.warn("uid为 {} 的通知在审核中，删除通知失败",uid);
            return body;
        }

        //TODO 删除对应的附件

        notificationRepository.deleteByUid(uid);

        body.setMessage("删除通知成功");
        return body;
    }

    @Override
    public RespBody update(NotificationAddDto dto){
        RespBody body = new RespBody();
        return body;
    }

    @Override
    public RespBody publish(String uid){
        RespBody body = new RespBody();
        return body;
    }

    @Override
    public RespBody page(UserDetailsImpl userDetails, NotificationPageDto pageDto){
        RespBody body = new RespBody();
        return body;
    }

    /**
     * 获取某一通知的细节
     *
     * @param uid 通知uid
     * @return
     */
    @Override
    public RespBody details(String uid){
        RespBody body = new RespBody();
        Notification notification = notificationRepository.findByUid(uid);
        if(notification == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该通知不存在");
            LOGGER.warn("uid为 {} 的通知不存在，不能提交审核",uid);
            return body;
        }

        NotificationDetailsVo vo = NotificationDetailsVo.convert(notification);
        body.setData(vo);

        body.setMessage("成功获取通知细节");
        return body;
    }



    /**
     * 后台管理员提交通知审核
     *
     * @param userDetails 用户信息
     * @param uid   通知uid
     * @return
     */
    @Override
    public RespBody toReview(UserDetailsImpl userDetails,String uid){
        RespBody body = new RespBody();
        Notification notification = notificationRepository.findByUid(uid);
        if(notification == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该通知不存在");
            LOGGER.warn("uid为 {} 的通知不存在，不能提交审核",uid);
            return body;
        }

        //后台管理员可以在通知创建后、被退回并修改后在再提交，此时状态为：DRAFT
        //也可以直接将审核不通过的通知再次提交审核，此时通知状态为：NOT_APPROVED
        if(notification.getStatus() != NotificationStatusEnum.DRAFT.ordinal() &&
                notification.getStatus() != NotificationStatusEnum.NOT_APPROVED.ordinal()){

            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("在[审核中][待发布][已发布]状态均不能提交审核");
            LOGGER.warn("uid为 {} 的通知不处于[草稿]或者[审核不通过]状态，固不能提交审核",uid);
            return body;
        }

        //TODO 查找与本账号同地区/镇的具有通知审核权限的用户

        //将状态设置为"审核中"
        notification.setStatus(NotificationStatusEnum.UNDER_REVIEW.ordinal());
        notificationRepository.save(notification);

        //TODO 向审核人推送消息

        body.setMessage("成功提交通知审核");
        return body;
    }

    /**
     * 审核人对通知进行审核
     * @param userDetails 用户信息
     * @param dto 通知审核参数封装对象
     * @return
     */
    @Override
    public RespBody review(UserDetailsImpl userDetails,NotificationReviewDto dto){
        RespBody body = new RespBody();
        Notification notification = notificationRepository.findByUid(dto.getUid());
        if (notification == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("指定的通知不存在");
            LOGGER.warn("uid为 {} 的通知不存在，审核通知失败",dto.getUid());
            return body;
        }

        if(notification.getStatus() != NewsStatusEnum.UNDER_REVIEW.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("指定的通知不在[审核中]状态");
            LOGGER.warn("uid为 {} 的通知不在[审核中]状态，审核通知失败",dto.getUid());
            return body;
        }

        //如果审核结果为:通过
        if(dto.isPass()){
            //将通知状态设置为"待发布"(可发布)状态
            notification.setStatus(NewsStatusEnum.RELEASABLE.ordinal());
        }else {
            //如果审核结果为:不通过

            //将通知状态设置为"不通过"状态
            notification.setStatus(NewsStatusEnum.NOT_APPROVED.ordinal());
        }

        //对通知的反馈意见
        notification.setFeedback(dto.getFeedback());

        //将当前用户记录为该通知的审核人
        Account currentAccount = accountRepository.findByUid(userDetails.getUsername());

        notification.setReviewer(NpcMemberUtil.getCurrentIden(userDetails.getLevel(),currentAccount.getNpcMembers()));

        notificationRepository.saveAndFlush(notification);

        body.setMessage("完成通知审核");
        return body;
    }
}
