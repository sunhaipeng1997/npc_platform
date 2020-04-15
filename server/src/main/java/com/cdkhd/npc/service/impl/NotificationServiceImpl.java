package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.*;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NotificationService;
import com.cdkhd.npc.service.NpcMemberRoleService;
import com.cdkhd.npc.service.PushMessageService;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.util.SysUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private NotificationRepository notificationRepository;
    private AttachmentRepository attachmentRepository;
    private NpcMemberRepository npcMemberRepository;
    private NpcMemberRoleService npcMemberRoleService;
    private AccountRepository accountRepository;
    private NotificationOpeRecordRepository notificationOpeRecordRepository;
    private NotificationViewDetailRepository notificationViewDetailRepository;
    private SystemSettingService systemSettingService;
    private PushMessageService pushMessageService;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, AttachmentRepository attachmentRepository, NpcMemberRepository npcMemberRepository, NpcMemberRoleService npcMemberRoleService, AccountRepository accountRepository, NotificationOpeRecordRepository notificationOpeRecordRepository, NotificationViewDetailRepository notificationViewDetailRepository, SystemSettingService systemSettingService, PushMessageService pushMessageService) {
        this.notificationRepository = notificationRepository;
        this.attachmentRepository = attachmentRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.npcMemberRoleService = npcMemberRoleService;
        this.accountRepository = accountRepository;
        this.notificationOpeRecordRepository = notificationOpeRecordRepository;
        this.notificationViewDetailRepository = notificationViewDetailRepository;
        this.systemSettingService = systemSettingService;
        this.pushMessageService = pushMessageService;
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

        //得到源文件名及扩展名
        String orgFilename = file.getOriginalFilename();
        String extName = FilenameUtils.getExtension(orgFilename);

        //生成新的文件名
        String newFilename = String.format("%s.%s", SysUtil.uid(), extName);

        //生成新文件的父目录路径
        String parentPath = String.format("static/public/notification");

        //创建新的文件
        File bgFile = new File(parentPath, newFilename);
        File parentFile = bgFile.getParentFile();
        if (!parentFile.exists()) {
            boolean mkdirs = parentFile.mkdirs();
            if (!mkdirs) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("系统内部错误");
                return body;
            }
        }

        // 拷贝文件
        try (InputStream is = file.getInputStream()) {
            FileUtils.copyInputStreamToFile(is, bgFile);
        } catch (IOException e) {
            LOGGER.error("通知附件保存失败 {}", e);
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("系统内部错误，存储文件失败");
            return body;
        }

        attachment.setName(orgFilename);
        attachment.setUrl(String.format("/public/notification/%s",newFilename));

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
        if(userDetails.getTown() != null){
            notification.setTown(userDetails.getTown());
        }else {
            notification.setTown(null);
        }
        notification.setLevel(userDetails.getLevel());

        if(!dto.isBillboard() && dto.getReceiversUid() == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该通知缺少接收人");
            LOGGER.warn("该通知缺少接收人");
            return body;
        }


        if(dto.getReceiversUid() == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("必须设置接收人");
            return body;
        }else {
            List<String> receiversUidList = JSONObject.parseArray(dto.getReceiversUid().toJSONString(),String.class);
            if(!receiversUidList.isEmpty()){
                Set<NpcMember> receivers = new HashSet<>();
                for (String npcMemberUid : receiversUidList){
                    NpcMember npcMember = npcMemberRepository.findByUid(npcMemberUid);
                    if(npcMember == null){
                        body.setStatus(HttpStatus.BAD_REQUEST);
                        body.setMessage("有不存在的接收人");
                        LOGGER.warn("uid为 {} 的接收人不存在，新增通知失败",npcMemberUid);
                        return body;
                    }
                    receivers.add(npcMember);
                }
                notification.setReceivers(receivers);
            }

            if (!receiversUidList.isEmpty()) {
                notification.setReceiversViewDetails(
                        receiversUidList.stream().map(receiverUid -> {
                            NpcMember receiver = npcMemberRepository.findByUid(receiverUid);
                            if (receiver != null) {
                                NotificationViewDetail viewDetail = new NotificationViewDetail();
                                viewDetail.setNotification(notification);
                                viewDetail.setIsRead(false);
                                viewDetail.setReceiver(receiver);

                                //notificationDetailRepository.saveAndFlush(detail);

                                return viewDetail;
                            }
                            return null;
                        }).filter(Objects::nonNull).collect(Collectors.toSet())
                );
            }
        }

        if(dto.getAttachmentsUid() != null){
            List<String> attachmentUidList = JSONObject.parseArray(dto.getAttachmentsUid().toJSONString(),String.class);
            if (!attachmentUidList.isEmpty()) {
                notification.setAttachments(
                        attachmentUidList.stream().map(attachmentUid -> {
                            Attachment attachment = attachmentRepository.findByUid(attachmentUid);
                            attachment.setNotification(notification);

                            // attachmentRepository.saveAndFlush(attachment);

                            return attachment;
                        }).collect(Collectors.toSet())
                );
            }
        }

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

        //删除之前的附件
        Set<Attachment> attachments = notification.getAttachments();
        if(!attachments.isEmpty()){
            for(Attachment attachment : attachments){
                File oldFile = new File("static", attachment.getUrl());
                if (oldFile.exists()) {
                    try {
                        FileUtils.forceDelete(oldFile);
                    } catch (IOException e) {
                        LOGGER.error("通知附件删除失败 {}", e);
                        body.setStatus(HttpStatus.BAD_REQUEST);
                        body.setMessage("系统内部错误");
                        return body;
                    }
                }
            }
        }

        notificationRepository.deleteByUid(uid);

        body.setMessage("删除通知成功");
        return body;
    }

    @Override
    public RespBody update(UserDetailsImpl userDetails,NotificationAddDto dto){
        RespBody body = new RespBody();

        if(dto.getUid().isEmpty()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("uid不能为空");
            return body;
        }

        Notification notification = notificationRepository.findByUid(dto.getUid());
        if(notification == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该通知不存在");
            LOGGER.warn("uid为 {} 的通知不存在，删除通知失败",dto.getUid());
            return body;
        }

        BeanUtils.copyProperties(dto, notification);

        //如果是一般通知，则必须要有接收人
        if(!dto.isBillboard() && dto.getReceiversUid().isEmpty()){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该通知缺少接收人");
            LOGGER.warn("该通知缺少接收人");
            return body;
        }

        if(!dto.getReceiversUid().isEmpty()){
            List<String> npcMemberUidList = JSONObject.parseArray(dto.getReceiversUid().toJSONString(),String.class);
            Set<NpcMember> receivers = new HashSet<>();
            for (String npcMemberUid : npcMemberUidList){
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
        }

        if(!dto.getAttachmentsUid().isEmpty()){
            List<String> attachmentUidList = JSONObject.parseArray(dto.getAttachmentsUid().toJSONString(),String.class);

//            List<Attachment> attachments = new ArrayList<>();
//            for (String attachmentUid : attachmentUidList){
//                Attachment attachment = attachmentRepository.findByUid(attachmentUid);
//                if(attachment == null){
//                    body.setStatus(HttpStatus.NOT_FOUND);
//                    body.setMessage("附件不存在");
//                    LOGGER.warn("uid为 {} 的附件不存在，新增通知失败",attachmentUid);
//                    return body;
//                }
//                attachments.add(attachment);
//            }
//            notification.setAttachments(attachments);
            if (!attachmentUidList.isEmpty()) {
                notification.setAttachments(
                        attachmentUidList.stream().map(attachmentUid -> {
                            Attachment attachment = attachmentRepository.findByUid(attachmentUid);
                            attachment.setNotification(notification);

                            // attachmentRepository.saveAndFlush(attachment);

                            return attachment;
                        }).collect(Collectors.toSet())
                );
            }
        }

        //保存数据
        notificationRepository.save(notification);

        body.setMessage("添加通知成功");
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

        NotificationOpeRecord notificationOpeRecord = new NotificationOpeRecord();
        notificationOpeRecord.setOriginalStatus(NotificationStatusEnum.DRAFT.ordinal());
        notificationOpeRecord.setResultStatus(NotificationStatusEnum.UNDER_REVIEW.ordinal());
        notificationOpeRecord.setFeedback(notification.getDepartment()+"发布:"+notification.getTitle());
        notificationOpeRecord.setAction("提交审核");
        notificationOpeRecord.setOperator(userDetails.getUsername());
        notificationOpeRecord.setNotification(notification);
        notificationOpeRecordRepository.saveAndFlush(notificationOpeRecord);

        //将状态设置为"审核中"
        notification.setStatus(NotificationStatusEnum.UNDER_REVIEW.ordinal());
        notification.setView(false);//审核人未读
        notification.getOpeRecords().add(notificationOpeRecord);
        notificationRepository.save(notification);

        String queryUid = new String();
        if(userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            queryUid = userDetails.getArea().getUid();
        }else {
            queryUid = userDetails.getTown().getUid();
        }

        //构造消息
        JSONObject notificationMsg = new JSONObject();
        notificationMsg.put("subtitle","收到一条待审核通知");
        notificationMsg.put("auditItem",notification.getTitle());
        if(userDetails.getTown() == null){
            notificationMsg.put("serviceType",userDetails.getArea().getName()+"通知");
        }else {
            notificationMsg.put("serviceType",userDetails.getArea().getName()+" "+userDetails.getTown().getName()+"通知");
        }
        notificationMsg.put("remarkInfo","来源:"+notification.getDepartment()+"<点击查看详情>");

        //查找与本账号同地区/镇的具有通知审核权限的用户
        List<NpcMember> reviewers =  npcMemberRoleService.findByKeyWordAndLevelAndUid(
                NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword(),userDetails.getLevel(),queryUid);

        //向审核人推送消息
        if(!reviewers.isEmpty()){
            for(NpcMember reviewer :reviewers){
                pushMessageService.pushMsg(reviewer.getAccount(),MsgTypeEnum.TO_AUDIT.ordinal(),notificationMsg);
            }
            body.setMessage("成功推送至审核人");
        }else {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("无通知审核人");
            return body;
        }

        return body;
    }


    /**
     * 后台管理员 或者 通知审核人 将通知公开
     *
     * @param uid 通知uid
     * @return
     */
    @Override
    public RespBody publish(UserDetailsImpl userDetails,String uid){
        RespBody body = new RespBody();
        Notification notification = notificationRepository.findByUid(uid);

        if (notification == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("指定的通知不存在");
            LOGGER.warn("uid为 {} 的通知不存在，发布通知失败",uid);
            return body;
        }

        if(notification.getStatus() != NotificationStatusEnum.RELEASABLE.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该通知还未审核通过，不可发布");
            LOGGER.warn("uid为 {} 的通知还未审核通过，发布通知失败",uid);
            return body;
        }

        if(notification.getPublished()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该通知已经公开，不可重复公开");
            LOGGER.warn("uid为 {} 的通知已经公开，不可重复设置为公开",uid);
            return body;
        }

        //添加操作记录
        NotificationOpeRecord notificationOpeRecord = new NotificationOpeRecord();
        notificationOpeRecord.setOriginalStatus(notification.getStatus());
        notificationOpeRecord.setResultStatus(NotificationStatusEnum.RELEASED.ordinal());
        notificationOpeRecord.setFeedback("完成发布"+notification.getTitle());
        notificationOpeRecord.setOpTime(new Date());
        notificationOpeRecord.setAction("发布");
        //将调用该接口的当前用户记录为该新闻的(操作者)
        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        notificationOpeRecord.setOperator(currentAccount.getUsername());
        notificationOpeRecord.setNotification(notificationRepository.findByUid(notification.getUid()));
        notificationOpeRecordRepository.saveAndFlush(notificationOpeRecord);

        //将状态设置为已发布
        notification.setStatus(NotificationStatusEnum.RELEASED.ordinal());
        //将新闻设置为公开状态
        notification.setPublished(true);
        notification.getOpeRecords().add(notificationOpeRecord);
        notificationRepository.saveAndFlush(notification);

        //构造消息
        JSONObject notificationMsg = new JSONObject();
        notificationMsg.put("subtitle","收到一条通知");
        notificationMsg.put("time",notification.getPublishAt());
        notificationMsg.put("theme",notification.getTitle());
        notificationMsg.put("remarkInfo","来源:"+notification.getDepartment()+"<点击查看详情>");

        Set<NpcMember> receivers = notification.getReceivers();
        if(!receivers.isEmpty()){
            for(NpcMember receiver:receivers){
                if(receiver.getAccount() != null){//只发送给已经注册的人，否则要报空指针异常
                    if(receiver.getAccount().getLoginWeChat() != null){
                        pushMessageService.pushMsg(receiver.getAccount(),MsgTypeEnum.CONFERENCE.ordinal(),notificationMsg);
                    }else {
                        continue;
                    }
                }else {
                    continue;
                }
            }
        }

        body.setMessage("通知公开发布成功");
        return body;
    }

    @Override
    public RespBody page(UserDetailsImpl userDetails, NotificationPageDto pageDto){
        //分页查询条件
        int begin = pageDto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()),
                pageDto.getProperty());

        //用户查询条件
        Specification<Notification> specification = (root, query, cb)->{
            List<Predicate> predicateList = new ArrayList<>();

            predicateList.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));

            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));

            if(userDetails.getTown() != null){
                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
            }

            //按签署部门查询
            if (StringUtils.isNotEmpty(pageDto.getDepartment())) {
                predicateList.add(cb.like(root.get("department").as(String.class), "%" + pageDto.getDepartment() + "%"));
            }

            //按通知标题模糊查询
            if (StringUtils.isNotEmpty(pageDto.getTitle())) {
                predicateList.add(cb.like(root.get("name").as(String.class), "%" + pageDto.getTitle() + "%"));
            }

            //按通知状态查询
            if (pageDto.getStatus() != null) {
                predicateList.add(cb.equal(root.get("status").as(Integer.class), pageDto.getStatus()));
            }

//            predicateList.add(cb.equal(root.get("isBillboard").as(Boolean.class), pageDto.isBillboard()));

//            predicateList.add(cb.equal(root.get("type").as(Byte.class), pageDto.getType()));

            //            predicateList.add(cb.equal(root.get("isBillboard").as(Boolean.class), pageDto.isBillboard()));

            if(pageDto.getType() != null){
                predicateList.add(cb.equal(root.get("type").as(Byte.class), pageDto.getType()));
            }

            return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
        };

        //查询数据库
        Page<Notification> page = notificationRepository.findAll(specification,pageable);

        //封装查询结果
        PageVo<NotificationPageVo> pageVo = new PageVo<>(page, pageDto);
        pageVo.setContent(page.getContent().stream().map(NotificationPageVo::convert).collect(Collectors.toList()));

        //返回数据
        RespBody<PageVo> body = new RespBody<>();
        body.setData(pageVo);

        return body;
    }

    /**
     * pc端获取某一通知的细节
     *
     * @param uid 通知uid
     * @return
     */
    @Override
    public RespBody details(String uid){
        RespBody body = new RespBody();
        if(uid.isEmpty()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("UID不能为空");
            return body;
        }

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



    @Override
    public RespBody publishForMobile(UserDetailsImpl userDetails,NotificationPublishDto dto){
        RespBody body = new RespBody();
        Notification notification = notificationRepository.findByUid(dto.getUid());

        if (notification == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("指定的通知不存在");
            LOGGER.warn("uid为 {} 的通知不存在，发布通知失败",dto.getUid());
            return body;
        }

        if(notification.getStatus() != NotificationStatusEnum.RELEASABLE.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该通知还未审核通过，不可发布");
            LOGGER.warn("uid为 {} 的通知还未审核通过，发布通知失败",dto.getUid());
            return body;
        }

        if(notification.getPublished()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该通知已经公开，不可重复公开");
            LOGGER.warn("uid为 {} 的通知已经公开，不可重复设置为公开",dto.getUid());
            return body;
        }

        //添加操作记录
        NotificationOpeRecord notificationOpeRecord = new NotificationOpeRecord();
        notificationOpeRecord.setOriginalStatus(notification.getStatus());
        notificationOpeRecord.setResultStatus(NotificationStatusEnum.RELEASED.ordinal());
        notificationOpeRecord.setFeedback("完成发布"+notification.getTitle());
        notificationOpeRecord.setOpTime(new Date());
        notificationOpeRecord.setAction("发布");
        //将调用该接口的当前用户记录为该新闻的(操作者)
        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        notificationOpeRecord.setOperator(NpcMemberUtil.getCurrentIden(dto.getLevel(),currentAccount.getNpcMembers()).getName());
        notificationOpeRecord.setNotification(notificationRepository.findByUid(notification.getUid()));
        notificationOpeRecordRepository.saveAndFlush(notificationOpeRecord);

        //将状态设置为已发布
        notification.setStatus(NotificationStatusEnum.RELEASED.ordinal());
        //将新闻设置为公开状态
        notification.setPublished(true);
        notification.getOpeRecords().add(notificationOpeRecord);
        notificationRepository.saveAndFlush(notification);

        //构造消息
        JSONObject notificationMsg = new JSONObject();
        notificationMsg.put("subtitle","收到一条通知");
        notificationMsg.put("time",notification.getPublishAt());
        notificationMsg.put("theme",notification.getTitle());
        notificationMsg.put("remarkInfo","来源:"+notification.getDepartment()+"<点击查看详情>");

        Set<NpcMember> receivers = notification.getReceivers();
        if(!receivers.isEmpty()){
            for(NpcMember receiver:receivers){
                if(receiver.getAccount() != null){//只发送给已经注册的人，否则要报空指针异常
                    if(receiver.getAccount().getLoginWeChat() != null){
                        pushMessageService.pushMsg(receiver.getAccount(),MsgTypeEnum.CONFERENCE.ordinal(),notificationMsg);
                    }else {
                        continue;
                    }
                }else {
                    continue;
                }
            }
        }

        body.setMessage("通知公开发布成功");
        return body;
    }


    //接收人获取通知详情
    @Override
    public RespBody detailsForMobileReceiver(UserDetailsImpl userDetails,String uid,Byte level){
        RespBody<NotificationMobileReceivedDetailsVo> body = new RespBody<>();
        if(uid.isEmpty()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("UID不能为空");
            return body;
        }

        Notification notification = notificationRepository.findByUid(uid);
        if(notification == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该通知不存在");
            LOGGER.warn("uid为 {} 的通知不存在",uid);
            return body;
        }

        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(level,currentAccount.getNpcMembers());

        if(!notification.getReceivers().contains(npcMember)){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该通知无此接收人");
            return body;
        }

        NotificationViewDetail viewDetail =  notificationViewDetailRepository.findByNotificationUidAndReceiverUid(notification.getUid(),npcMember.getUid());
        if(viewDetail == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该通知接收人无查看记录");
            return body;
        }

        //记录已读
        viewDetail.setIsRead(true);
        notificationViewDetailRepository.saveAndFlush(viewDetail);


        NotificationMobileReceivedDetailsVo vo = NotificationMobileReceivedDetailsVo.convert(notification);
        body.setData(vo);

        body.setMessage("成功获取通知细节");
        return body;
    }


    //审核人获取通知详情
    @Override
    public RespBody detailsForMobileReviewer(UserDetailsImpl userDetails,String uid,Byte level){
        RespBody<NotificationDetailsForMobileVo> body = new RespBody<>();
        if(uid.isEmpty()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("UID不能为空");
            return body;
        }

        Notification notification = notificationRepository.findByUid(uid);
        if(notification == null){
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("该通知不存在");
            LOGGER.warn("uid为 {} 的通知不存在",uid);
            return body;
        }

        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(level,currentAccount.getNpcMembers());

        List<String> roleKeywords = npcMember.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toList());
        if (roleKeywords.contains(NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword()) ) {
            notification.setView(true);
        }else {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("您没有通知审核权限");
            return body;
        }
        notificationRepository.saveAndFlush(notification);

        NotificationDetailsForMobileVo vo = NotificationDetailsForMobileVo.convert(notification);

        body.setData(vo);
        body.setMessage("成功获取通知细节");
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

        if(notification.getStatus() != NotificationStatusEnum.UNDER_REVIEW.ordinal() && notification.getStatus() != NotificationStatusEnum.NOT_APPROVED.ordinal()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("指定的通知不在[审核中][不通过]状态");
            LOGGER.warn("uid为 {} 的通知不在[审核中][不通过]状态，审核通知失败",dto.getUid());
            return body;
        }

        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(dto.getLevel(),currentAccount.getNpcMembers());
        if(npcMember == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("不存在该代表");
            LOGGER.warn("不存在该代表");
            return body;
        }

        //添加操作记录
        NotificationOpeRecord notificationOpeRecord = new NotificationOpeRecord();
        notificationOpeRecord.setOriginalStatus(notification.getStatus());

        //如果审核结果为:通过
        if(dto.getPass()){
            //将通知状态设置为"待发布"(可发布)状态
            notification.setStatus(NotificationStatusEnum.RELEASABLE.ordinal());
            notificationOpeRecord.setResultStatus(NotificationStatusEnum.RELEASABLE.ordinal());
        }else {
            //如果审核结果为:不通过

            //将通知状态设置为"不通过"状态
            notification.setStatus(NotificationStatusEnum.NOT_APPROVED.ordinal());
            notificationOpeRecord.setResultStatus(NotificationStatusEnum.NOT_APPROVED.ordinal());
        }
        //对新闻的反馈意见
        notificationOpeRecord.setFeedback(dto.getFeedback());
        //将调用该接口的当前用户记录为该新闻的审核人(操作者)
        notificationOpeRecord.setOperator(npcMember.getName());
        notificationOpeRecord.setAction("审核");
        //先查出来再关联，确保不会报瞬态错误
        notificationOpeRecord.setNotification(notificationRepository.findByUid(notification.getUid()));
        notificationOpeRecordRepository.saveAndFlush(notificationOpeRecord);

        notification.setView(true);
        notification.getOpeRecords().add(notificationOpeRecord);
        notificationRepository.saveAndFlush(notification);


        String queryUid = new String();
        if(dto.getLevel().equals(LevelEnum.AREA.getValue())){
            queryUid = userDetails.getArea().getUid();
        }else {
            queryUid = userDetails.getTown().getUid();
        }

        //查找与本账号同地区/镇的具有通知审核权限的用户
        List<NpcMember> reviewers =  npcMemberRoleService.findByKeyWordAndLevelAndUid(
                NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword(),dto.getLevel(),queryUid);

        //构造消息
        JSONObject notificationMsg = new JSONObject();
        notificationMsg.put("subtitle","收到一条通知的审核结果");
        notificationMsg.put("auditItem",notification.getTitle());
        notificationMsg.put("result",dto.getPass()?"通过(可发布)":"不通过(驳回修改)");
        notificationMsg.put("remarkInfo","操作人："+ notificationOpeRecord.getOperator()+"<点击查看详情>");

        for(NpcMember reviewer:reviewers){
            if(!reviewer.getAccount().getUid().equals(userDetails.getUid())){
                pushMessageService.pushMsg(reviewer.getAccount(),MsgTypeEnum.AUDIT_RESULT.ordinal(),notificationMsg);
            }
        }

        body.setMessage("完成通知审核");
        return body;
    }


    /**
     * 收到通知 分页查询
     * @param dto 分页信息封装对象
     * @return
     */
    @Override
    public RespBody mobileReceivedPage(UserDetailsImpl userDetails, NotificationPageDto dto) {

        RespBody<PageVo<NotificationMobileReceivedPageVo>> body = new RespBody<>();

        //分页查询条件
        int begin = dto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());

        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(dto.getLevel(),currentAccount.getNpcMembers());

        if (npcMember != null) {

            //用户查询条件
            Specification<NotificationViewDetail> specification = (root, query, cb)->{
                List<Predicate> predicateList = new ArrayList<>();
                predicateList.add(cb.equal(root.get("receiver").get("uid"), npcMember.getUid()));
                predicateList.add(cb.isTrue(root.get("notification").get("published")));
                predicateList.add(cb.equal(root.get("notification").get("status").as(Integer.class),NotificationStatusEnum.RELEASED.ordinal()));

                predicateList.add(cb.equal(root.get("notification").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                predicateList.add(cb.equal(root.get("notification").get("level").as(Byte.class), dto.getLevel()));

                if(userDetails.getTown() != null){
                    if(dto.getLevel().equals(LevelEnum.TOWN.getValue())) {
                        predicateList.add(cb.equal(root.get("notification").get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                    }
                }
                return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
            };
            //查询数据库
            Page<NotificationViewDetail> page = notificationViewDetailRepository.findAll(specification,pageable);

            //封装查询结果
            PageVo<NotificationMobileReceivedPageVo> pageVo = new PageVo<>(page, dto);
            pageVo.setContent(page.getContent().stream().map(NotificationMobileReceivedPageVo::convert).collect(Collectors.toList()));

            //返回数据
            body.setData(pageVo);
            return body;
        }

        body.setStatus(HttpStatus.BAD_REQUEST);
        body.setMessage("不存在该代表");
        return body;
    }


    @Override
    public RespBody mobileReviewPage(UserDetailsImpl userDetails,NotificationPageDto dto) {

        RespBody<PageVo<NotificationPageVo>> body = new RespBody<>();

        //暂时不允许审核人查询查稿状态的通知
        if (dto.getStatus() !=null){
            if(dto.getStatus() == NotificationStatusEnum.CREATED.ordinal() || dto.getStatus() == NotificationStatusEnum.DRAFT.ordinal()) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("您不能查询草稿状态通知");
                return body;
            }
        }

        int begin = dto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());

        Account currentAccount = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(dto.getLevel(),currentAccount.getNpcMembers());

        if (npcMember != null) {
            List<String> roleKeywords = npcMember.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toList());

            //如果是通知公告审核人
            if (roleKeywords.contains(NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword()) ) {
                
                //用户查询条件
                Specification<Notification> specification = (root, query, cb)->{
                    List<Predicate> predicateList = new ArrayList<>();

                    predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                    predicateList.add(cb.equal(root.get("level").as(Byte.class), dto.getLevel()));

                    if(userDetails.getTown() != null){
                        if(dto.getLevel().equals(LevelEnum.TOWN.getValue())) {
                            predicateList.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                        }
                    }

                    //按通知状态查询
                    if (dto.getStatus() != null) {
                        predicateList.add(cb.equal(root.get("status").as(Integer.class), dto.getStatus()));
                    }else {
                        predicateList.add(cb.notEqual(root.get("status").as(Integer.class), NotificationStatusEnum.CREATED.ordinal()));
                        predicateList.add(cb.notEqual(root.get("status").as(Integer.class), NotificationStatusEnum.DRAFT.ordinal()));
                    }

                    return query.where(predicateList.toArray(new Predicate[0])).getRestriction();
                };

                //查询数据库
                Page<Notification> page = notificationRepository.findAll(specification,pageable);

                //封装查询结果
                PageVo<NotificationPageVo> pageVo = new PageVo<>(page, dto);
                pageVo.setContent(page.getContent().stream().map(NotificationPageVo::convert).collect(Collectors.toList()));

                //返回数据
                body.setData(pageVo);
            }else {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("您暂无此权限");
                return body;
            }
        }
        return body;
    }

    @Override
    public void downloadAttachment(HttpServletResponse response, UserDetailsImpl uds, String uid) {
        Attachment attachment = attachmentRepository.findByUid(uid);

        // 找不到指定的附件
        if (attachment == null) return;

        String filePath = attachment.getUrl();
        File file = new File("static", filePath);
        // 文件不存在
        if (!file.exists()) return;

        try (
                FileInputStream fis = new FileInputStream(file);
                OutputStream os = response.getOutputStream()
        ) {
            response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String fileName = new String(attachment.getName().getBytes("UTF-8"), "iso-8859-1");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");

            IOUtils.copyLarge(fis, os);
        } catch (IOException e) {
            LOGGER.error("附件下载出错！\n{}", e);
        }
    }

}
