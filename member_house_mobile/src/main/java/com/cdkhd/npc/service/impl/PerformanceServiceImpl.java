package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.PerformanceListVo;
import com.cdkhd.npc.entity.vo.PerformanceVo;
import com.cdkhd.npc.enums.*;
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
import java.util.*;
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
        //区上或者街道，统一使用区上的履职类型
        if (performanceTypeDto.getLevel().equals(LevelEnum.AREA.getValue()) || (performanceTypeDto.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))) {
            performanceTypeList = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (performanceTypeDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
            performanceTypeList = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(), userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
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
        this.scanPerformance(npcMember);
        int begin = performancePageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, performancePageDto.getSize());
        Page<Performance> performancePage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("npcMember").get("uid").as(String.class), npcMember.getUid()));
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
            predicates.add(cb.equal(root.get("level").as(Byte.class), npcMember.getLevel()));
            if (performancePageDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
            }
            //状态 1未审核  2已审核
            if (performancePageDto.getStatus() != null) {
                if (performancePageDto.getStatus().equals(StatusEnum.ENABLED.getValue())) {//未审核(前端定义的查询状态  1未审核  2 已审核)
                    predicates.add(cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.SUBMITTED_AUDIT.getValue()));
                } else {//已审核
                    Predicate success = cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.AUDIT_SUCCESS.getValue());
                    Predicate failed = cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.AUDIT_FAILURE.getValue());
                    Predicate or = cb.or(success,failed);
                    predicates.add(or);
                }
            }
            Predicate[] p = new Predicate[predicates.size()];
            query.where(cb.and(predicates.toArray(p)));
            query.orderBy(cb.asc(root.get("myView")),cb.asc(root.get("status")),cb.desc(root.get("workAt")));
            return query.getRestriction();
        }, page);
        List<PerformanceListVo> performanceVos = performancePage.getContent().stream().map(PerformanceListVo::convert).collect(Collectors.toList());
        PageVo<PerformanceListVo> vo = new PageVo<>(performancePage, performancePageDto);
        vo.setContent(performanceVos);
        body.setData(vo);
        return body;
    }

    //扫描我的所有履职，判断出哪些可以操作
    private void scanPerformance(NpcMember member) {
        //获取我提出的所有履职
        List<Performance> performanceList = performanceRepository.findByNpcMemberUidAndIsDelFalse(member.getUid());
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.MINUTE, -5);// 5分钟之前的时间
        Date beforeDate = beforeTime.getTime();
        for (Performance performance : performanceList) {
            //未审核且未查看且未超过5分钟
            if (performance.getStatus().equals(PerformanceStatusEnum.SUBMITTED_AUDIT.getValue()) && performance.getCreateTime().after(beforeDate) && !performance.getView()){
                performance.setCanOperate(true);
//            }else if (performance.getStatus().equals(PerformanceStatusEnum.REVOKE.getValue()) || performance.getStatus().equals(PerformanceStatusEnum.AUDIT_FAILURE.getValue())){
//                //撤回了可以操作、审核不通过可以操作
//                performance.setCanOperate(true);
            }else {
                //其他情况都不可操作
                performance.setCanOperate(false);
            }
        }
        performanceRepository.saveAll(performanceList);
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
            if (performance == null) {//表示修改第一次提交
                performance = performanceRepository.findByUid(addPerformanceDto.getUid());//第一次提交履职，我们根据uid查询出来
                Set<PerformanceImage> oldImages = performance.getPerformanceImages();//找出所有旧照片
                if (CollectionUtils.isNotEmpty(oldImages)){
                    performanceImageRepository.deleteAll(oldImages);//删除所有旧照片
                }
                performance.setTransUid(addPerformanceDto.getTransUid());//把新的transUid存进去
            }
        }
        PerformanceType performanceType = performanceTypeRepository.findByUid(addPerformanceDto.getPerformanceType());
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
            JSONObject performanceMsg = new JSONObject();
            //构造消息
            performanceMsg.put("subtitle","收到一条待审核的履职信息。");
            performanceMsg.put("auditItem",npcMember.getName()+" 代表提出的 "+addPerformanceDto.getTitle() +" 履职信息");
            performanceMsg.put("serviceType",performanceType.getName());
            for (NpcMember auditor : auditors) {
                if (auditor.getAccount() != null) {
                    //给对应的接受代表推送服务号信息
                    performanceMsg.put("remarkInfo", "审核人： " + auditor.getName() + "  <点击查看详情>");
                    pushMessageService.pushMsg(auditor.getAccount(), MsgTypeEnum.TO_AUDIT.ordinal(), performanceMsg);
                }
            }
        }
        performance.setStatus(PerformanceStatusEnum.SUBMITTED_AUDIT.getValue());//设置为待审核状态
        performance.setView(false);
        performance.setCanOperate(true);//添加和修改的时候，可以进行操作
        performance.setPerformanceType(performanceType);
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
        performance.setView(true);
        performance.setStatus(PerformanceStatusEnum.AUDIT_SUCCESS.getValue());//默认已通过
        if (addPerformanceDto.getLevel().equals(LevelEnum.AREA.getValue()) || (addPerformanceDto.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))){
            performance.setPerformanceType(performanceTypeRepository.findByNameAndLevelAndAreaUidAndIsDelFalse(addPerformanceDto.getPerformanceType(), addPerformanceDto.getLevel(), npcMember.getArea().getUid()));
        }else if (addPerformanceDto.getLevel().equals(LevelEnum.TOWN.getValue())){
            performance.setPerformanceType(performanceTypeRepository.findByNameAndLevelAndTownUidAndIsDelFalse(addPerformanceDto.getPerformanceType(), addPerformanceDto.getLevel(), npcMember.getTown().getUid()));
        }
        performance.setTitle(addPerformanceDto.getTitle());
        performance.setWorkAt(addPerformanceDto.getWorkAt());
        performance.setContent(addPerformanceDto.getContent());
        performance.setAuditAt(new Date());
        performance.setReason(addPerformanceDto.getReason());
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
        //再删除履职信息
        performance.setIsDel(true);
        performanceRepository.saveAndFlush(performance);
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
        Sort.Order viewSort = new Sort.Order(Sort.Direction.ASC, "view");//先按查看状态排序
        Sort.Order statusSort = new Sort.Order(Sort.Direction.ASC, "status");//先按状态排序
        Sort.Order createAt = new Sort.Order(Sort.Direction.DESC, "createTime");//再按创建时间排序
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(viewSort);
        orders.add(statusSort);
        orders.add(createAt);
        Sort sort = Sort.by(orders);

        Pageable page = PageRequest.of(begin, performancePageDto.getSize(), sort);
        Page<Performance> performancePage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class),performancePageDto.getLevel()));
            //撤回状态不展示
            predicates.add(cb.notEqual(root.get("status").as(Byte.class), PerformanceStatusEnum.REVOKE.getValue()));
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
                    List<String> uids = npcMemberList.stream().filter(member -> !member.getIsDel() && member.getStatus().equals(StatusEnum.ENABLED.getValue())).map(NpcMember::getUid).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(uids)){
                        CriteriaBuilder.In<Object> in = cb.in(root.get("npcMember").get("uid"));
                        in.value(uids);
                        predicates.add(in);
                    }
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
                    List<String> uids = npcMemberList.stream().filter(member -> !member.getIsDel() && member.getStatus().equals(StatusEnum.ENABLED.getValue())).map(NpcMember::getUid).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(uids)) {
                        CriteriaBuilder.In<Object> in = cb.in(root.get("npcMember").get("uid"));
                        in.value(uids);
                        predicates.add(in);
                    }
                }
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
            }
            //状态 1未审核  2已审核
            if (performancePageDto.getStatus() != null) {
                if (performancePageDto.getStatus().equals(StatusEnum.ENABLED.getValue())) {//未审核（前端定义的状态）
                    predicates.add(cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.SUBMITTED_AUDIT.getValue()));
                } else {//已审核
                    Predicate success = cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.AUDIT_SUCCESS.getValue());
                    Predicate failed = cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.AUDIT_FAILURE.getValue());
                    Predicate or = cb.or(success,failed);
                    predicates.add(or);
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
        if (performance.getStatus().equals(PerformanceStatusEnum.AUDIT_SUCCESS.getValue()) || performance.getStatus().equals(PerformanceStatusEnum.AUDIT_FAILURE.getValue())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该条履职已经审核过了，不能再审核");
            LOGGER.error("该条履职已经审核过了，不能再审核");
            return body;
        }
        Account auditor = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(performance.getLevel(), auditor.getNpcMembers());
        performance.setStatus(auditPerformanceDto.getStatus());
        performance.setReason(auditPerformanceDto.getReason());
        performance.setMyView(false);
        performance.setAuditAt(new Date());
        performance.setAuditor(npcMember);
        performanceRepository.saveAndFlush(performance);
        Account account = performance.getNpcMember().getAccount();//无论审核通不通过，都通知代表一声
        if (account != null){
            JSONObject performanceMsg = new JSONObject();
            performanceMsg.put("subtitle","您的履职有了审核结果，请前往小程序查看。");
            performanceMsg.put("auditItem",performance.getTitle());
            performanceMsg.put("result", PerformanceStatusEnum.getName(auditPerformanceDto.getStatus()));
            performanceMsg.put("remarkInfo","审核人："+ performance.getAuditor().getName()+" <点击查看详情>");
            pushMessageService.pushMsg(account, MsgTypeEnum.AUDIT_RESULT.ordinal(),performanceMsg);
        }
        return body;
    }

    @Override
    public RespBody performanceList(UidDto uidDto) {
        RespBody body = new RespBody();
        int begin = uidDto.getPage() - 1;
        NpcMember npcMember = npcMemberRepository.findByUid(uidDto.getUid());
        Pageable page = PageRequest.of(begin, uidDto.getSize(), Sort.Direction.fromString(uidDto.getDirection()), uidDto.getProperty());
        Page<Performance> performancePage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("npcMember").get("mobile").as(String.class), npcMember.getMobile()));
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.AUDIT_SUCCESS.getValue()));
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        List<PerformanceListVo> performanceVos = performancePage.getContent().stream().map(PerformanceListVo::convert).collect(Collectors.toList());
        PageVo<PerformanceListVo> vo = new PageVo<>(performancePage, uidDto);
        vo.setContent(performanceVos);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody revokePerformance(String uid) {
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

        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.MINUTE, -5);// 5分钟之前的时间
        Date beforeDate = beforeTime.getTime();

        if (performance.getStatus().equals(PerformanceStatusEnum.SUBMITTED_AUDIT.getValue()) && performance.getCreateTime().before(beforeDate) && performance.getView()){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该条履职已超过5分钟，或审核人员已查看，不能撤回");
            LOGGER.error("该条履职已超过5分钟，或审核人员已查看，不能撤回");
            return body;
        }else if (performance.getStatus().equals(PerformanceStatusEnum.REVOKE.getValue())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该条履职已经撤回了，请勿再次撤回");
            LOGGER.error("该条履职已经撤回了，请勿再次撤回");
            return body;
        }
        performance.setCanOperate(false);
        performance.setStatus(PerformanceStatusEnum.REVOKE.getValue());
        performanceRepository.saveAndFlush(performance);
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
