package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.PerformanceListVo;
import com.cdkhd.npc.entity.vo.PerformanceVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.NpcMemberRoleEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.member_house.PerformanceImageRepository;
import com.cdkhd.npc.repository.member_house.PerformanceRepository;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.service.NpcMemberRoleService;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.service.PushMessageService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerformanceServiceImpl implements PerformanceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceServiceImpl.class);

    private PerformanceRepository performanceRepository;

    private PerformanceTypeRepository performanceTypeRepository;

    private SystemSettingRepository systemSettingRepository;

    private NpcMemberRepository npcMemberRepository;

    private AccountRepository accountRepository;

    private PerformanceImageRepository performanceImageRepository;

    private NpcMemberRoleService npcMemberRoleService;

    private PushMessageService pushMessageService;

    @Autowired
    public PerformanceServiceImpl(PerformanceRepository performanceRepository, PerformanceTypeRepository performanceTypeRepository, SystemSettingRepository systemSettingRepository, NpcMemberRepository npcMemberRepository, AccountRepository accountRepository, PerformanceImageRepository performanceImageRepository, NpcMemberRoleService npcMemberRoleService, PushMessageService pushMessageService) {
        this.performanceRepository = performanceRepository;
        this.performanceTypeRepository = performanceTypeRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.accountRepository = accountRepository;
        this.performanceImageRepository = performanceImageRepository;
        this.npcMemberRoleService = npcMemberRoleService;
        this.pushMessageService = pushMessageService;
    }

    /**
     * 履职类型列表
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody performanceTypes(MobileUserDetailsImpl userDetails, PerformanceTypeDto performanceTypeDto) {
        RespBody body = new RespBody();
        List<PerformanceType> performanceTypeList = Lists.newArrayList();
        performanceTypeList = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalse(performanceTypeDto.getLevel(), userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        if (performanceTypeDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
            performanceTypeList = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalse(performanceTypeDto.getLevel(), userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> types = performanceTypeList.stream().map(type -> CommonVo.convert(type.getUid(), type.getName())).collect(Collectors.toList());
        body.setData(types);
        return body;
    }

    @Override
    public RespBody performancePage(MobileUserDetailsImpl userDetails, PerformancePageDto performancePageDto) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(performancePageDto.getLevel(), account.getNpcMembers());
        int begin = performancePageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, performancePageDto.getSize(), Sort.Direction.fromString(performancePageDto.getDirection()), performancePageDto.getProperty());
        Page<Performance> performancePage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("npcMember").get("uid").as(String.class), npcMember.getUid()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
            predicates.add(cb.equal(root.get("level").as(Byte.class), npcMember.getLevel()));
            if (performancePageDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
            }
            //状态 1未审核  2已审核
            if (performancePageDto.getStatus() != null) {
                if (performancePageDto.getStatus().equals(StatusEnum.ENABLED.getValue())) {//未审核
                    predicates.add(cb.isNull(root.get("status")));
                } else {//已审核
                    predicates.add(cb.isNotNull(root.get("status")));
                }
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        List<PerformanceListVo> performanceVos = performancePage.getContent().stream().map(PerformanceListVo::convert).collect(Collectors.toList());
        PageVo<PerformanceListVo> vo = new PageVo<>(performancePage, performancePageDto);
        vo.setContent(performanceVos);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody performanceDetail(ViewDto viewDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(viewDto.getUid())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("uid为空");
            return body;
        }
        Performance performance = performanceRepository.findByUid(viewDto.getUid());
        if (null == performance) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("根据uid查询出的实体为空");
            return body;
        }
        //我查看审核人给我回复的消息，消除红点
        if (null != viewDto.getType() && viewDto.getType().equals(StatusEnum.ENABLED.getValue())){
            performance.setMyView(true);
            performanceRepository.saveAndFlush(performance);
        }else if (null != viewDto.getType() && viewDto.getType().equals(StatusEnum.DISABLED.getValue())) {
            performance.setView(true);
            performanceRepository.saveAndFlush(performance);
        }
        PerformanceVo performanceVo = PerformanceVo.convert(performance);
        body.setData(performanceVo);
        return body;
    }

    /**
     * 添加或修改履职
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody addOrUpdatePerformance(MobileUserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(addPerformanceDto.getLevel(), account.getNpcMembers());
        //当前用户是否为工作在当前区镇的代表
        if (npcMember == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("您不是代表该区/镇的代表，无法添加履职");
            return body;
        }
        Performance performance;
        //验证履职uid参数是否传过来了
        if (StringUtils.isEmpty(addPerformanceDto.getUid())) {
            //没有uid表示添加履职
            //查询是否是的第一次提交
            performance = performanceRepository.findByTransUid(addPerformanceDto.getTransUid());
        } else {
            //有uid表示修改履职
            //查询是否是的第一次提交
            performance = performanceRepository.findByUidAndTransUid(addPerformanceDto.getUid(), addPerformanceDto.getTransUid());

        }
        if (performance == null) {//如果是第一次提交，就保存基本信息
            performance = new Performance();
            performance.setLevel(addPerformanceDto.getLevel());
            performance.setArea(npcMember.getArea());
            performance.setTown(npcMember.getTown());
            performance.setTransUid(addPerformanceDto.getTransUid());
            performance.setNpcMember(npcMember);
            //设置完了基本信息后，给相应的审核人员推送消息
            List<NpcMember> auditors = Lists.newArrayList();
            SystemSetting systemSetting = systemSettingRepository.findAll().get(0);
            //如果是在镇上履职，那么查询镇上的审核人员
            String uid;
            if (addPerformanceDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
                uid = npcMember.getTown().getUid();
            } else {
                uid = npcMember.getArea().getUid();
            }
            //首先判断端当前用户的角色是普通代表还是小组审核人员还是总审核人员
            if (systemSetting.getPerformanceGroupAudit()) {//开启了小组审核人员
                List<String> permissions = npcMemberRoleService.findKeyWordByUid(npcMember.getUid(), false);
                if (CollectionUtils.isNotEmpty(permissions) && permissions.contains(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword())) {//如果当前登录人是履职小组审核人，那么查询履职总审核人
                    auditors = npcMemberRoleService.findByKeyWordAndLevelAndUid(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword(), addPerformanceDto.getLevel(), uid);
                } else {
                    //如果不是小组审核人
                    //查询出所有的小组审核人
                    List<NpcMember> allGroupAuditors = npcMemberRoleService.findByKeyWordAndUid(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword(), addPerformanceDto.getLevel(), uid);
                    //查询与我同组的所有小组审核人
                    for (NpcMember allGroupAuditor : allGroupAuditors) {
                        if (addPerformanceDto.getLevel().equals(LevelEnum.TOWN.getValue()) && allGroupAuditor.getNpcMemberGroup().getUid().equals(npcMember.getNpcMemberGroup().getUid())) {
                            auditors.add(allGroupAuditor);
                        } else if (addPerformanceDto.getLevel().equals(LevelEnum.AREA.getValue()) && allGroupAuditor.getTown().getUid().equals(npcMember.getTown().getUid())) {
                            auditors.add(allGroupAuditor);
                        }
                    }
                }
            } else {
                //小组审核人员没有开启，那么直接有总审核人员审核
                auditors = npcMemberRoleService.findByKeyWordAndLevelAndUid(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword(), addPerformanceDto.getLevel(), uid);
            }
            for (NpcMember auditor : auditors) {
                //给对应的接受代表推送服务号信息
                JSONObject performanceMsg = new JSONObject();
                performanceMsg.put("subtitle","您有一条新的消息，请前往小程序查看。");
                performanceMsg.put("accountName",auditor.getName());
                performanceMsg.put("mobile",auditor.getMobile());
                performanceMsg.put("content",addPerformanceDto.getTitle());
                performanceMsg.put("remarkInfo","点击进入小程序查看详情");
//                pushMessageService.pushMsg(auditor.getAccount(), MsgTypeEnum.TO_AUDIT.ordinal(),performanceMsg);
            }
        }
        performance.setPerformanceType(performanceTypeRepository.findByUid(addPerformanceDto.getPerformanceType()));
        performance.setTitle(addPerformanceDto.getTitle());
        performance.setWorkAt(addPerformanceDto.getWorkAt());
        performance.setContent(addPerformanceDto.getContent());
        performanceRepository.saveAndFlush(performance);

        if (addPerformanceDto.getImage() != null) {//有附件，就保存附件信息
            this.saveCover(addPerformanceDto.getImage(), performance);
        }
        return body;
    }

    @Override
    public RespBody addPerformanceFormSug(MobileUserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto, Set<SuggestionImage> suggestionImages) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(addPerformanceDto.getLevel(), account.getNpcMembers());
        Performance performance = new Performance();
        performance.setLevel(addPerformanceDto.getLevel());
        performance.setArea(npcMember.getArea());
        performance.setTown(npcMember.getTown());
        performance.setNpcMember(npcMemberRepository.findByUid(addPerformanceDto.getUid()));//提出人，就存在uid里面
        performance.setAuditor(npcMember);//审核人
        performance.setStatus(StatusEnum.ENABLED.getValue());//默认已通过
        performance.setPerformanceType(performanceTypeRepository.findByUid(addPerformanceDto.getPerformanceType()));
        performance.setTitle(addPerformanceDto.getTitle());
        performance.setWorkAt(addPerformanceDto.getWorkAt());
        performance.setContent(addPerformanceDto.getContent());
        Set<PerformanceImage> performanceImages = new HashSet<>();
        for (SuggestionImage suggestionImage : suggestionImages){
            PerformanceImage performanceImage = new PerformanceImage();
            performanceImage.setPerformance(performance);
            performanceImage.setUrl(suggestionImage.getUrl());
            performanceImages.add(performanceImage);
        }
        performanceImageRepository.saveAll(performanceImages);
        performance.setPerformanceImages(performanceImages);
        performanceRepository.saveAndFlush(performance);

//        if (addPerformanceDto.getImage() != null) {//有附件，就保存附件信息
//            this.saveCover(addPerformanceDto.getImage(), performance);
//        }
        return body;
    }

    @Override
    public RespBody deletePerformance(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("uid为空");
            return body;
        }
        Performance performance = performanceRepository.findByUid(uid);
        if (null == performance) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("根据uid查询出的实体为空");
            return body;
        }
        if (!performance.getStatus().equals(StatusEnum.DISABLED)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("不能删除该条履职");
            LOGGER.error("履职状态不是审核失败，不能删除");
            return body;
        }
        //先删除履职照片
        List<PerformanceImage> performanceImages = performanceImageRepository.findByPerformanceUid(uid);
        performanceImageRepository.deleteAll(performanceImages);
        //再删除履职信息
        performanceRepository.delete(performance);
        return body;
    }

    @Override
    public RespBody performanceAuditorPage(MobileUserDetailsImpl userDetails, PerformancePageDto performancePageDto) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(performancePageDto.getLevel(), account.getNpcMembers());//当前登录账户的代表信息
        List<String> roleKeywords = npcMember.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toList());
        SystemSetting systemSetting = this.getSystemSetting(userDetails, performancePageDto);//系统设置开关

        //排序条件
        int begin = performancePageDto.getPage() - 1;
        Sort.Order statusSort = new Sort.Order(Sort.Direction.ASC, "status");//先按状态排序
        Sort.Order createAt = new Sort.Order(Sort.Direction.DESC, "createTime");//再按创建时间排序
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(statusSort);
        orders.add(createAt);
        Sort sort = Sort.by(orders);

        Pageable page = PageRequest.of(begin, performancePageDto.getSize(), sort);
        Page<Performance> performancePage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (performancePageDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
                List<NpcMember> npcMemberList = Lists.newArrayList();
                if (roleKeywords.contains(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword()) && systemSetting.getPerformanceGroupAudit()) {
                    //todo 如果是小组审核人人员，并且开关打开了，才查询同组的所有代表
                    npcMemberList = npcMemberRepository.findByNpcMemberGroupUidAndIsDelFalse(npcMember.getNpcMemberGroup().getUid());
                    predicates.add(cb.notEqual(root.get("npcMember").get("uid").as(String.class), npcMember.getUid()));//小组审核人的话，只需要在本组排除自己就行
                } else if (roleKeywords.contains(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword()) && systemSetting.getPerformanceGroupAudit()) {
                    //todo 如果总审核人员，开关开启，那么查询所有小组审核人员
                    npcMemberList = npcMemberRoleService.findByKeyWordAndLevelAndUid(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword(), performancePageDto.getLevel(), npcMember.getTown().getUid());
                    predicates.add(cb.notEqual(root.get("npcMember").get("uid").as(String.class), npcMember.getUid()));//小组审核人的话，只需要在本组排除自己就行
                } else if (roleKeywords.contains(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword()) && !systemSetting.getPerformanceGroupAudit()) {
                    //todo 如果是总审核人员，并且开关关闭了，那么查询所有的
                    npcMemberList = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(npcMember.getTown().getUid(), performancePageDto.getLevel());
                }
                if (CollectionUtils.isNotEmpty(npcMemberList)) {
                    List<String> uids = npcMemberList.stream().map(NpcMember::getUid).collect(Collectors.toList());
                    CriteriaBuilder.In<Object> in = cb.in(root.get("npcMember").get("uid"));
                    in.value(uids);
                    predicates.add(in);
                }
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
            } else if (performancePageDto.getLevel().equals(LevelEnum.AREA.getValue())) {
                List<NpcMember> npcMemberList = Lists.newArrayList();
                if (roleKeywords.contains(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword()) && systemSetting.getPerformanceGroupAudit()) {
                    //todo 如果是小组审核人人员，并且开关打开了，才查询同镇的所有代表
                    npcMemberList = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(npcMember.getTown().getUid(), performancePageDto.getLevel());
                    predicates.add(cb.notEqual(root.get("npcMember").get("uid").as(String.class), npcMember.getUid()));//小组审核人的话，只需要在本组排除自己就行
                } else if (roleKeywords.contains(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword()) && systemSetting.getPerformanceGroupAudit()) {
                    //todo 如果总审核人员，开关开启，那么查询所有镇审核人员
                    npcMemberList = npcMemberRoleService.findByKeyWordAndLevelAndUid(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword(), performancePageDto.getLevel(), npcMember.getArea().getUid());
                    predicates.add(cb.notEqual(root.get("npcMember").get("uid").as(String.class), npcMember.getUid()));//小组审核人的话，只需要在本组排除自己就行
                } else if (roleKeywords.contains(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword()) && !systemSetting.getPerformanceGroupAudit()) {
                    //todo 如果是总审核人员，并且开关关闭了，那么查询所有的
                    npcMemberList = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(npcMember.getArea().getUid(), performancePageDto.getLevel());
                }
                if (CollectionUtils.isNotEmpty(npcMemberList)) {
                    List<String> uids = npcMemberList.stream().map(NpcMember::getUid).collect(Collectors.toList());
                    CriteriaBuilder.In<Object> in = cb.in(root.get("npcMember").get("uid"));
                    in.value(uids);
                    predicates.add(in);
                }
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
            }
            //状态 已回复  未回复
            if (performancePageDto.getStatus() != null) {
                if (performancePageDto.getStatus() == 1) {
                    predicates.add(cb.isNull(root.get("status").as(Byte.class)));
                } else if (performancePageDto.getStatus() == 2) {
                    predicates.add(cb.isNotNull(root.get("status").as(Byte.class)));
                }
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        List<PerformanceListVo> performanceVos = performancePage.getContent().stream().map(PerformanceListVo::convert).collect(Collectors.toList());
        PageVo<PerformanceListVo> vo = new PageVo<>(performancePage, performancePageDto);
        vo.setContent(performanceVos);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody auditPerformance(MobileUserDetailsImpl userDetails, AuditPerformanceDto auditPerformanceDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(auditPerformanceDto.getUid())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("uid为空");
            return body;
        }
        Performance performance = performanceRepository.findByUid(auditPerformanceDto.getUid());
        if (null == performance) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("根据uid查询出的实体为空");
            return body;
        }
        if (null != performance.getStatus() && (performance.getStatus().equals(StatusEnum.DISABLED) || performance.getStatus().equals(StatusEnum.ENABLED))) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该条履职已经审核过了，不能再审核");
            LOGGER.error("该条履职已经审核过了，不能再审核");
            return body;
        }
        Account auditor = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(performance.getLevel(), auditor.getNpcMembers());
        performance.setStatus(auditPerformanceDto.getStatus());
        performance.setReason(auditPerformanceDto.getReason());
        performance.setAuditor(npcMember);
//        if (auditPerformanceDto.getStatus().equals(StatusEnum.ENABLED)) {//审核通过
            Account account = performance.getNpcMember().getAccount();//无论审核通不通过，都通知代表一声
//            pushMessageService.pushMsg(account, "", 1, "");
            JSONObject performanceMsg = new JSONObject();
            performanceMsg.put("subtitle","您有一条新的消息，请前往小程序查看。");
            performanceMsg.put("accountName",performance.getNpcMember().getName());
            performanceMsg.put("mobile",performance.getNpcMember().getMobile());
            performanceMsg.put("content",performance.getTitle());
            performanceMsg.put("remarkInfo","点击进入小程序查看详情");
//            pushMessageService.pushMsg(account, MsgTypeEnum.AUDIT_RESULT.ordinal(),performanceMsg);
//        }
        return body;
    }

    @Override
    public RespBody performanceList(UidDto uidDto) {
        RespBody body = new RespBody();
        int begin = uidDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, uidDto.getSize(), Sort.Direction.fromString(uidDto.getDirection()), uidDto.getProperty());
        Page<Performance> performancePage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("npcMember").get("uid").as(String.class), uidDto.getUid()));
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        List<PerformanceListVo> performanceVos = performancePage.getContent().stream().map(PerformanceListVo::convert).collect(Collectors.toList());
        body.setData(performanceVos);
        return body;
    }


    public void saveCover(MultipartFile cover, Performance performance) {
        //保存图片到文件系统
        String url = ImageUploadUtil.saveImage("performanceImage", cover, 500, 500);
        if (url.equals("error")) {
            LOGGER.error("保存图片到文件系统失败");
        }
        //保存图片到数据库
        PerformanceImage performanceImage = new PerformanceImage();
        performanceImage.setUrl(url);
        performanceImage.setTransUid(performance.getTransUid());
        performanceImage.setPerformance(performance);
        performanceImageRepository.saveAndFlush(performanceImage);
    }

    public SystemSetting getSystemSetting(MobileUserDetailsImpl userDetails, PerformancePageDto performancePageDto) {
        SystemSetting systemSetting = new SystemSetting();
        if (performancePageDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndTownUid(performancePageDto.getLevel(), userDetails.getTown().getUid());
        } else if (performancePageDto.getLevel().equals(LevelEnum.AREA.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndAreaUid(performancePageDto.getLevel(), userDetails.getArea().getUid());
        }
        return systemSetting;
    }

}
