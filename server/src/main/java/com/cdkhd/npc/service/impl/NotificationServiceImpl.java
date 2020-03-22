package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.NewsPageVo;
import com.cdkhd.npc.entity.vo.NotificationDetailsVo;
import com.cdkhd.npc.entity.vo.NotificationPageVo;
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
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            body.setMessage("系统内部错误");
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
        List<Attachment> attachments = new ArrayList<>();
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

        //删除之前的附件
        List<Attachment> attachments = notification.getAttachments();
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
    public RespBody update(NotificationAddDto dto){
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
            List<Attachment> attachments = new ArrayList<>();
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
        }

        //保存数据
        notificationRepository.save(notification);

        body.setMessage("添加通知成功");
        return body;
    }

    @Override
    public RespBody publish(String uid){
        RespBody body = new RespBody();
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

//            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));

//            if(userDetails.getTown() != null){
//                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
//            }

            //按签署部门查询
            if (StringUtils.isNotEmpty(pageDto.getDepartment())) {
                predicateList.add(cb.like(root.get("department").as(String.class), "%" + pageDto.getDepartment() + "%"));
            }

            //按新闻标题模糊查询
            if (StringUtils.isNotEmpty(pageDto.getTitle())) {
                predicateList.add(cb.like(root.get("name").as(String.class), "%" + pageDto.getTitle() + "%"));
            }

            //按通知状态查询
            if (pageDto.getStatus() != null) {
                predicateList.add(cb.equal(root.get("status").as(Integer.class), pageDto.getStatus()));
            }

            predicateList.add(cb.equal(root.get("isBillboard").as(Boolean.class), pageDto.isBillboard()));

            predicateList.add(cb.equal(root.get("type").as(Byte.class), pageDto.getType()));

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

    @Override
    public RespBody pageForMobile(NotificationPageDto pageDto){
        //分页查询条件
        int begin = pageDto.getPage() - 1;
        Pageable pageable = PageRequest.of(begin, pageDto.getSize(),
                Sort.Direction.fromString(pageDto.getDirection()),
                pageDto.getProperty());

        //用户查询条件
        Specification<Notification> specification = (root, query, cb)->{
            List<Predicate> predicateList = new ArrayList<>();

//            predicateList.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));

//            predicateList.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));

//            if(userDetails.getTown() != null){
//                predicateList.add(cb.equal(root.get("town").get("uid").as(String.class),userDetails.getTown().getUid()));
//            }

            //按签署部门查询
            if (StringUtils.isNotEmpty(pageDto.getDepartment())) {
                predicateList.add(cb.like(root.get("department").as(String.class), "%" + pageDto.getDepartment() + "%"));
            }

            //按新闻标题模糊查询
            if (StringUtils.isNotEmpty(pageDto.getTitle())) {
                predicateList.add(cb.like(root.get("name").as(String.class), "%" + pageDto.getTitle() + "%"));
            }

            //按通知状态查询
            if (pageDto.getStatus() != null) {
                predicateList.add(cb.equal(root.get("status").as(Integer.class), pageDto.getStatus()));
            }

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
     * 获取某一通知的细节
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
