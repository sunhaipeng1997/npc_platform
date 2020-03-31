package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.SuggestionVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.MobileSugStatusEnum;
import com.cdkhd.npc.enums.PerformanceTypeEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionImageRepository;
import com.cdkhd.npc.repository.member_house.SuggestionReplyRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.service.SuggestionService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SuggestionServiceImpl implements SuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestionServiceImpl.class);

    private final SuggestionRepository suggestionRepository;

    private final AccountRepository accountRepository;

    private final SuggestionImageRepository suggestionImageRepository;

    private final SuggestionBusinessRepository suggestionBusinessRepository;

    private final SuggestionReplyRepository suggestionReplyRepository;

    private final PerformanceService performanceService;

    private final Environment env;

    @Autowired
    public SuggestionServiceImpl(SuggestionRepository suggestionRepository, AccountRepository accountRepository, SuggestionImageRepository suggestionImageRepository, NpcMemberRepository npcMemberRepository, SuggestionBusinessRepository suggestionBusinessRepository, SuggestionReplyRepository suggestionReplyRepository, PerformanceService performanceService, Environment env) {
        this.suggestionRepository = suggestionRepository;
        this.accountRepository = accountRepository;
        this.suggestionImageRepository = suggestionImageRepository;
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.suggestionReplyRepository = suggestionReplyRepository;
        this.performanceService = performanceService;
        this.env = env;
    }

    /**
     * 建议类型列表
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody sugBusList(MobileUserDetailsImpl userDetails, SuggestionBusinessDto dto) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> sb = Lists.newArrayList();
        if (dto.getLevel().equals(LevelEnum.AREA.getValue())){
            sb = suggestionBusinessRepository.findByLevelAndAreaUidAndIsDelFalse(dto.getLevel(),userDetails.getArea().getUid());
        }else if (dto.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sb = suggestionBusinessRepository.findByLevelAndTownUidAndIsDelFalse(dto.getLevel(),userDetails.getTown().getUid());
        }
        List<CommonVo> commonVos = sb.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public RespBody npcMemberSug(MobileUserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody<PageVo<SuggestionVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(dto.getLevel(), account.getNpcMembers());
        PageVo<SuggestionVo> vo = new PageVo<>(dto);
        if (npcMember != null) {
            Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                predicates.add(cb.equal(root.get("level").as(Byte.class), npcMember.getLevel()));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (dto.getLevel().equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                if (dto.getStatus() != null){
                    if (dto.getStatus().equals(MobileSugStatusEnum.TO_BE_AUDITED.getValue())){  //未审核
                        predicates.add(cb.equal(root.get("status").as(Byte.class), MobileSugStatusEnum.TO_BE_AUDITED.getValue()));
                    }else if (dto.getStatus().equals(MobileSugStatusEnum.HAS_BEEN_AUDITED.getValue())){  //已审核
                        predicates.add(cb.equal(root.get("status").as(Byte.class), MobileSugStatusEnum.HAS_BEEN_AUDITED.getValue()));
                    }
                }
                return query.where(predicates.toArray(new Predicate[0])).getRestriction();
            }, page);
            vo.setContent(pageRes.stream().map(SuggestionVo::convert).collect(Collectors.toList()));
            vo.copy(pageRes);
        }
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody addOrUpdateSuggestion(MobileUserDetailsImpl userDetails, SuggestionAddDto dto) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(dto.getLevel(), account.getNpcMembers());

        //判断当前用户是否为工作在当前区镇的代表
        if (npcMember == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("您不是该区/镇的代表，无法添加建议");
            return body;
        }

        Suggestion suggestion;
        //验证建议uid参数是否传过来了
        if (StringUtils.isEmpty(dto.getUid())) {
            //没有uid表示添加建议
            //查询是否是的第一次提交
            suggestion = suggestionRepository.findByTransUid(dto.getTransUid());
        } else {
            //有uid表示修改建议
            //查询是否是的第一次提交
            suggestion = suggestionRepository.findByUidAndTransUid(dto.getUid(), dto.getTransUid());
        }
        if (suggestion == null) { //如果是第一次提交，就保存基本信息
            suggestion = new Suggestion();
            suggestion.setLevel(dto.getLevel());
            suggestion.setArea(npcMember.getArea());
            suggestion.setTown(npcMember.getTown());
            suggestion.setRaiser(npcMember);
            suggestion.setLeader(npcMember);
            suggestion.setStatus(SuggestionStatusEnum.SUBMITTED_AUDIT.getValue());  //建议状态改为“已提交待审核”
            //设置完基本信息后，给相应审核人员推送消息
//            List<NpcMember> auditors;
//            if (dto.getLevel().equals(LevelEnum.TOWN.getValue())) {
//                //如果是在镇上提建议，那么查询镇上的审核人员
//                //首先判断端当前用户的角色是普通代表还是小组审核人员还是总审核人员
//                Set<String> keyword = Sets.newHashSet();//权限的集合
//                SystemSetting systemSetting =
            //判断当前代表的权限
//                if (keyword.contains("小组履职审核权限") )
//            } else {
//                //如果是在区上提建议，那么查询区上的审核人员
//
//            }
        }
        suggestion.setTitle(dto.getTitle());
        suggestion.setContent(dto.getContent());
        suggestion.setRaiser(npcMember);
        suggestion.setRaiseTime(dto.getRaiseTime());
        SuggestionBusiness suggestionBusiness = suggestionBusinessRepository.findByUid(dto.getBusiness());
        if (suggestionBusiness == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("建议类型不能为空");
            return body;
        }
        suggestion.setSuggestionBusiness(suggestionBusiness);
        suggestionRepository.saveAndFlush(suggestion);

        if (dto.getImage() != null) {  //有附件，保存附件信息
            this.saveCover(dto.getImage(), suggestion);
        }
        return body;
    }

    public void saveCover(MultipartFile cover, Suggestion suggestion) {
        //保存图片到文件系统
        String url = ImageUploadUtil.saveImage("suggestionImage", cover, 500, 500);
        if (url.equals("error")) {
            LOGGER.error("保存图片到文件系统失败");
        }
        //保存图片到数据库
        SuggestionImage suggestionImage = new SuggestionImage();
        suggestionImage.setUrl(url);
        suggestionImage.setSuggestion(suggestion);
        suggestionImageRepository.saveAndFlush(suggestionImage);
    }

    @Override
    public RespBody deleteSuggestion(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("请先选择一条建议");
        }
        Suggestion suggestion = suggestionRepository.findByUid(uid);
        suggestion.setDel(true);  //逻辑删除
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }

    @Override
    public RespBody suggestionDetail(String uid) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(uid);
        if (suggestion == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该条建议");
            return body;
        }
        SuggestionVo suggestionVo = SuggestionVo.convert(suggestion);
        body.setData(suggestionVo);
        return body;
    }

    @Override
    public RespBody audit(MobileUserDetailsImpl userDetails, SuggestionAuditDto suggestionAuditDto) {
        //提建议的时候审核通过了，要加到履职里面去
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(suggestionAuditDto.getUid());
        if (suggestion == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("建议不存在");
            return body;
        }

        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(suggestionAuditDto.getLevel(), account.getNpcMembers());
        suggestion.setStatus(SuggestionStatusEnum.SELF_HANDLE.getValue());  //将建议状态设置成“自行办理”
        suggestion.setAuditReason(suggestionAuditDto.getReason());
        suggestion.setAuditTime(new Date());
        suggestion.setAuditor(npcMember);
        suggestionRepository.saveAndFlush(suggestion);

        //生成一条履职
        AddPerformanceDto addPerformanceDto = new AddPerformanceDto();
        addPerformanceDto.setContent(suggestion.getContent());
        addPerformanceDto.setLevel(suggestionAuditDto.getLevel());
        addPerformanceDto.setPerformanceType(PerformanceTypeEnum.SUGGESTION.getValue());
        addPerformanceDto.setTitle(suggestion.getTitle());
        addPerformanceDto.setWorkAt(suggestion.getRaiseTime());
        addPerformanceDto.setUid(suggestion.getRaiser().getUid());
        Set<SuggestionImage> suggestionImages = suggestion.getSuggestionImages();
        performanceService.addPerformanceFormSug(userDetails, addPerformanceDto, suggestionImages);
        return body;
    }

    @Override
    public RespBody suggestionRevoke(String uid) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(uid);
        if (suggestion == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("建议不存在");
            return body;
        }
        int timeout = 0;
        String time = env.getProperty("code.overdueTime");  //获取配置文件中设置的允许撤回时长
        if (StringUtils.isNotBlank(time)) {
            timeout = Integer.parseInt(time);
        }
        Date expireAt = DateUtils.addMinutes(suggestion.getCreateTime(), timeout);  //过期时间

        int view = suggestion.getView();
        if (expireAt.before(new Date()) || view == 1) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("已超出撤回消息时间或该建议已被查看，无法撤回！！");
            return body;
        }
        suggestion.setCanOperate(true);
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }


    @Override
    public RespBody auditorSug(MobileUserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody<PageVo<SuggestionVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        PageVo<SuggestionVo> vo = new PageVo<>(dto);
        Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            Predicate predicate = root.isNotNull();
            predicate = cb.and(predicate, cb.equal(root.get("raiser").get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (dto.getStatus() != null){
                if (dto.getStatus().equals(MobileSugStatusEnum.All.getValue())){  //未审核
                    predicate = cb.and(predicate,cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue()));
                }else if (dto.getStatus().equals(MobileSugStatusEnum.TO_BE_AUDITED.getValue())){  //已审核
                    Predicate or = cb.or(cb.equal(root.get("status"), SuggestionStatusEnum.SELF_HANDLE.getValue()),cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.AUDIT_FAILURE.getValue()));
                    predicate = cb.and(predicate,or);
                }
            }
            return predicate;
        }, page);
        vo.setContent(pageRes.stream().map(SuggestionVo::convert).collect(Collectors.toList()));
        vo.copy(pageRes);
        body.setData(vo);
        return body;
    }

    /**
     * 获取该代表提出的审核通过所有建议
     * @param uid
     * @return
     */
    @Override
    public RespBody getMemberSugList(String uid, SuggestionPageDto dto) {
        RespBody<PageVo<SuggestionVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize(), Sort.Direction.fromString(dto.getDirection()), dto.getProperty());
        PageVo<SuggestionVo> vo = new PageVo<>(dto);
        Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("raiser").get("uid").as(String.class), uid);
            predicate = cb.and(predicate, cb.equal(root.get("status").as(Byte.class), MobileSugStatusEnum.HAS_BEEN_AUDITED.getValue()));
            return predicate;
        }, page);
        vo.setContent(pageRes.stream().map(SuggestionVo::convert).collect(Collectors.toList()));
        vo.copy(pageRes);
        body.setData(vo);
        return body;
    }
}
