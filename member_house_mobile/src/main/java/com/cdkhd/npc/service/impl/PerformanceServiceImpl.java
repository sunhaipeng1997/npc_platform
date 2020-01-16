package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.AddPerformanceDto;
import com.cdkhd.npc.entity.dto.AuditPerformanceDto;
import com.cdkhd.npc.entity.dto.PerformancePageDto;
import com.cdkhd.npc.entity.vo.PerformanceListVo;
import com.cdkhd.npc.entity.vo.PerformanceVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.NpcMemberRoleRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.member_house.PerformanceImageRepository;
import com.cdkhd.npc.repository.member_house.PerformanceRepository;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.service.NpcMemberRoleService;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.service.PushService;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
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

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerformanceServiceImpl implements PerformanceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceServiceImpl.class);

    private PerformanceRepository performanceRepository;

    private PerformanceTypeRepository performanceTypeRepository;

    private SystemSettingRepository systemSettingRepository;

    private NpcMemberRepository npcMemberRepository;

    private NpcMemberRoleRepository npcMemberRoleRepository;

    private AccountRepository accountRepository;

    private PerformanceImageRepository performanceImageRepository;

    private NpcMemberRoleService npcMemberRoleService;

    private PushService pushService;

    private SystemSettingService systemSettingService;


    @Autowired
    public PerformanceServiceImpl(PerformanceRepository performanceRepository, PerformanceTypeRepository performanceTypeRepository, SystemSettingRepository systemSettingRepository, NpcMemberRepository npcMemberRepository, NpcMemberRoleRepository npcMemberRoleRepository, AccountRepository accountRepository, PerformanceImageRepository performanceImageRepository, NpcMemberRoleService npcMemberRoleService, PushService pushService, SystemSettingService systemSettingService) {
        this.performanceRepository = performanceRepository;
        this.performanceTypeRepository = performanceTypeRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.npcMemberRoleRepository = npcMemberRoleRepository;
        this.accountRepository = accountRepository;
        this.performanceImageRepository = performanceImageRepository;
        this.npcMemberRoleService = npcMemberRoleService;
        this.pushService = pushService;
        this.systemSettingService = systemSettingService;
    }

    /**
     * 履职类型列表
     * @param userDetails
     * @return
     */
    @Override
    public RespBody performanceTypeList(UserDetailsImpl userDetails, PerformanceType performanceType) {
        RespBody body = new RespBody();
        List<PerformanceType> performanceTypeList = Lists.newArrayList();
        if (performanceType.getLevel().equals(LevelEnum.TOWN.getValue())) {
            performanceTypeList = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalse(performanceType.getLevel(),userDetails.getTown().getUid(),StatusEnum.ENABLED.getValue());
        }else if (performanceType.getLevel().equals(LevelEnum.AREA.getValue())){
            performanceTypeList = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalse(performanceType.getLevel(),userDetails.getArea().getUid(),StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> types = performanceTypeList.stream().map(type -> CommonVo.convert(type.getUid(),type.getName())).collect(Collectors.toList());
        body.setData(types);
        return body;
    }

    @Override
    public RespBody performancePage(UserDetailsImpl userDetails, PerformancePageDto performancePageDto) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(performancePageDto.getLevel(), account.getNpcMembers());
        int begin = performancePageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, performancePageDto.getSize(), Sort.Direction.fromString(performancePageDto.getDirection()), performancePageDto.getProperty());
        Page<Performance> performancePage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("npcMember").get("uid").as(String.class), npcMember.getUid()));
            if (performancePageDto.getLevel().equals(LevelEnum.TOWN.getValue())){
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), performancePageDto.getAreaUid()));
            }else if (performancePageDto.getLevel().equals(LevelEnum.AREA.getValue())){
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), performancePageDto.getAreaUid()));
            }
            //状态 已回复  未回复
            if (performancePageDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("status").as(Byte.class), performancePageDto.getStatus()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        List<PerformanceListVo> performanceVos = performancePage.getContent().stream().map(PerformanceListVo::convert).collect(Collectors.toList());
        body.setData(performanceVos);
        return body;
    }

    @Override
    public RespBody performanceDetail(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("uid为空");
            return body;
        }
        Performance performance = performanceRepository.findByUid(uid);
        if (null == performance){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("根据uid查询出的实体为空");
            return body;
        }
        PerformanceVo performanceVo = PerformanceVo.convert(performance);
        body.setData(performanceVo);
        return body;
    }

    /**
     * 添加或修改履职
     * @param userDetails
     * @return
     */
    @Override
    public RespBody addOrUpdatePerformance(UserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto) {
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
        }else{
            //有uid表示修改履职
            //查询是否是的第一次提交
            performance = performanceRepository.findByUidAndTransUid(addPerformanceDto.getUid(), addPerformanceDto.getTransUid());

        }
        if (performance == null) {//如果是第一次提交，就保存基本信息
            performance = new Performance();
            performance.setLevel(addPerformanceDto.getLevel());
            performance.setArea(npcMember.getArea());
            performance.setTown(npcMember.getTown());
            performance.setNpcMember(npcMember);
            //设置完了基本信息后，给相应的审核人员推送消息
            List<NpcMember> auditors;
            SystemSetting systemSetting = systemSettingRepository.findAll().get(0);
            //如果是在镇上履职，那么查询镇上的审核人员
            //首先判断端当前用户的角色是普通代表还是小组审核人员还是总审核人员
            if (systemSetting.getPerformanceGroupAudit()) {//开启了小组审核人员
                List<String> permissions = npcMemberRoleService.findKeyWordByUid(npcMember.getUid());
                if (permissions.contains("lvxzshr")){//如果当前登录人是履职小组审核人，那么查询履职总审核人
                    auditors = npcMemberRoleService.findByKeyWordAndLevelAndUid("lvzsh",addPerformanceDto.getLevel(),npcMember.getTown().getUid());
                }
                else {
                    //如果不是小组审核人
                    //查询与我同组的所有小组审核人
                    auditors = npcMemberRoleService.findByKeyWordAndUid("lvxzsh", addPerformanceDto.getLevel() ,npcMember.getTown().getUid());
                }
            }else{
                //小组审核人员没有开启，那么直接有总审核人员审核
                auditors = npcMemberRoleService.findByKeyWordAndLevelAndUid("lvzsh",addPerformanceDto.getLevel(),npcMember.getTown().getUid());
            }
            for (NpcMember auditor : auditors) {//todo 推送消息得重寫
                pushService.pushMsg(auditor.getAccount(),"",1,"");
            }
        }
        performance.setPerformanceType(performanceTypeRepository.findByUid(addPerformanceDto.getPerformanceType()));
        performance.setTitle(addPerformanceDto.getTitle());
        performance.setWorkAt(addPerformanceDto.getWorkAt());
        performance.setContent(addPerformanceDto.getContent());
        performanceRepository.saveAndFlush(performance);

        if (addPerformanceDto.getImage() != null) {//有附件，就保存附件信息
            this.saveCover(addPerformanceDto.getImage(),performance);
        }

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
        if (null == performance){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("根据uid查询出的实体为空");
            return body;
        }
        if (!performance.getStatus().equals(StatusEnum.DISABLED)){
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
    public RespBody performanceAuditorPage(UserDetailsImpl userDetails, PerformancePageDto performancePageDto) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(performancePageDto.getLevel(), account.getNpcMembers());
        SystemSetting systemSetting = systemSettingService.getSystemSetting();
        int begin = performancePageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, performancePageDto.getSize(), Sort.Direction.fromString(performancePageDto.getDirection()), performancePageDto.getProperty());
        Page<Performance> performancePage = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (performancePageDto.getLevel().equals(LevelEnum.TOWN.getValue())){
                if ("判断角色".equals(11)) {
                    //todo 如果是小组审核人人员，并且开关打开了，才查询
                    //todo 如果小组审核人员，开关关闭，那么结果为空
                    //todo 如果是总审核人员，并且开关关闭了，那么查询所有的
                    //todo 如果总审核人员，开关关闭，那么查询所有小组审核人员
                }
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), performancePageDto.getAreaUid()));
            }else if (performancePageDto.getLevel().equals(LevelEnum.AREA.getValue())){
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), performancePageDto.getAreaUid()));
            }
            //状态 已回复  未回复
            if (performancePageDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("status").as(Byte.class), performancePageDto.getStatus()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        List<PerformanceListVo> performanceVos = performancePage.getContent().stream().map(PerformanceListVo::convert).collect(Collectors.toList());
        body.setData(performanceVos);
        return body;
    }

    @Override
    public RespBody auditPerformance(UserDetailsImpl userDetails, AuditPerformanceDto auditPerformanceDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(auditPerformanceDto.getUid())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("uid为空");
            return body;
        }
        Performance performance = performanceRepository.findByUid(auditPerformanceDto.getUid());
        if (null == performance){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条履职");
            LOGGER.error("根据uid查询出的实体为空");
            return body;
        }
        if (performance.getStatus().equals(StatusEnum.DISABLED) || performance.getStatus().equals(StatusEnum.ENABLED)){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该条履职已经审核过了，不能再审核");
            LOGGER.error("该条履职已经审核过了，不能再审核");
            return body;
        }
        performance.setStatus(auditPerformanceDto.getStatus());
        performance.setReason(auditPerformanceDto.getReason());
        if (auditPerformanceDto.getStatus().equals(StatusEnum.ENABLED)){//审核通过
            Account account = performance.getNpcMember().getAccount();
            pushService.pushMsg(account,"",1,"");
        }
        return body;
    }


    public void saveCover(MultipartFile cover,Performance performance){
        //保存图片到文件系统
        String url = ImageUploadUtil.saveImage("experienceImage", cover,500,500);
        if (url.equals("error")) {
            LOGGER.error("保存图片到文件系统失败");
        }
        //保存图片到数据库
        PerformanceImage performanceImage = new PerformanceImage();
        performanceImage.setUrl(url);
        performanceImage.setPerformance(performance);
        performanceImageRepository.saveAndFlush(performanceImage);
    }
}
