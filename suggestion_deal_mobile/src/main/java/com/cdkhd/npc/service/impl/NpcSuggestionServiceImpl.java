package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.SugDetailVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.NpcSugStatusEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.GovernmentUserRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionImageRepository;
import com.cdkhd.npc.repository.member_house.SuggestionReplyRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.*;
import com.cdkhd.npc.service.NpcSuggestionService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class NpcSuggestionServiceImpl implements NpcSuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcSuggestionServiceImpl.class);

    private final AccountRepository accountRepository;

    private final SuggestionRepository suggestionRepository;

    private final SuggestionBusinessRepository suggestionBusinessRepository;

    private final SuggestionImageRepository suggestionImageRepository;

    private final SuggestionReplyRepository suggestionReplyRepository;

    private final AppraiseRepository appraiseRepository;

    private final ResultRepository resultRepository;

    private final UnitSuggestionRepository unitSuggestionRepository;

    private final UnitRepository unitRepository;

    private final GovernmentUserRepository governmentUserRepository;

    private final SecondedRepository secondedRepository;

    private final UrgeRepository urgeRepository;

    private final SuggestionSettingRepository suggestionSettingRepository;

    @Autowired
    public NpcSuggestionServiceImpl(AccountRepository accountRepository,
                                    SuggestionRepository suggestionRepository,
                                    SuggestionBusinessRepository suggestionBusinessRepository,
                                    SuggestionImageRepository suggestionImageRepository,
                                    SuggestionReplyRepository suggestionReplyRepository,
                                    AppraiseRepository appraiseRepository,
                                    ResultRepository resultRepository,
                                    UnitSuggestionRepository unitSuggestionRepository,
                                    UnitRepository unitRepository,
                                    GovernmentUserRepository governmentUserRepository, SecondedRepository secondedRepository, UrgeRepository urgeRepository, SuggestionSettingRepository suggestionSettingRepository) {
        this.accountRepository = accountRepository;
        this.suggestionRepository = suggestionRepository;
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.suggestionImageRepository = suggestionImageRepository;
        this.suggestionReplyRepository = suggestionReplyRepository;
        this.appraiseRepository = appraiseRepository;
        this.resultRepository = resultRepository;
        this.unitSuggestionRepository = unitSuggestionRepository;
        this.unitRepository = unitRepository;
        this.governmentUserRepository = governmentUserRepository;
        this.secondedRepository = secondedRepository;
        this.urgeRepository = urgeRepository;
        this.suggestionSettingRepository = suggestionSettingRepository;
    }

    @Override
    public RespBody sugBusList(MobileUserDetailsImpl userDetails, SugBusDto sugBusDto) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> sb = Lists.newArrayList();
        if (sugBusDto.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sb = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(), userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        } else if (sugBusDto.getLevel().equals(LevelEnum.AREA.getValue()) ||
                (sugBusDto.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))) {
            //区上或者是街道统一使用区上的建议类型
            sb = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(), userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = sb.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public RespBody addSuggestion(MobileUserDetailsImpl userDetails, SugAddDto sugAddDto) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(sugAddDto.getLevel(), account.getNpcMembers());

        //判断当前用户是否为工作在当前区镇的代表
        if (npcMember == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("您不是该区/镇的代表，无法添加建议");
            return body;
        }

        Suggestion suggestion = suggestionRepository.findByTransUid(sugAddDto.getTransUid());
        if (null == suggestion) {  //说明该建议是第一次提交，保存基本信息
            suggestion = new Suggestion();
            suggestion.setLevel(sugAddDto.getLevel());
            suggestion.setArea(npcMember.getArea());
            suggestion.setTown(npcMember.getTown());
            suggestion.setRaiser(npcMember);
            suggestion.setLeader(npcMember);
            suggestion.setTransUid(sugAddDto.getTransUid());
            suggestion.setTitle(sugAddDto.getTitle());
            suggestion.setContent(sugAddDto.getContent());
            suggestion.setRaiser(npcMember);
            suggestion.setRaiseTime(new Date());
            suggestion.setView(false);
            suggestion.setCanOperate(true);
            suggestion.setStatus(sugAddDto.getStatus());
            SuggestionBusiness suggestionBusiness = suggestionBusinessRepository.findByUid(sugAddDto.getBusiness());
            if (suggestionBusiness == null) {
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("建议类型不能为空");
                return body;
            }
            suggestion.setSuggestionBusiness(suggestionBusiness);
            suggestionRepository.saveAndFlush(suggestion);
        }
        if (sugAddDto.getImage() != null) {  //有附件，保存附件信息
            this.saveCover(sugAddDto.getImage(), suggestion);
        }
        return body;
    }

    @Override
    public RespBody updateSuggestion(SugAddDto sugAddDto) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugAddDto.getUid());
        if (suggestion == null) {
            body.setMessage("此建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        suggestion = suggestionRepository.findByUidAndTransUid(sugAddDto.getUid(), sugAddDto.getTransUid());
        if (null == suggestion) {  //第一次提交且有附件就删除之前的图片
            suggestion = suggestionRepository.findByUid(sugAddDto.getUid());
            Set<SuggestionImage> images = suggestion.getSuggestionImages();
            suggestionImageRepository.deleteAll(images);
        }
        suggestion.setTransUid(sugAddDto.getTransUid());
        suggestion.setCanOperate(true);
        suggestion.setStatus(sugAddDto.getStatus());
        suggestion.setRaiseTime(new Date());
        suggestion.setTitle(sugAddDto.getTitle());
        suggestion.setContent(sugAddDto.getContent());
        SuggestionBusiness suggestionBusiness = suggestionBusinessRepository.findByUid(sugAddDto.getBusiness());
        if (suggestionBusiness == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("建议类型不能为空");
            return body;
        }
        suggestion.setSuggestionBusiness(suggestionBusiness);
        suggestionRepository.saveAndFlush(suggestion);
        if (sugAddDto.getImage() != null) {  //有附件，保存附件信息
            this.saveCover(sugAddDto.getImage(), suggestion);
        }
        return body;
    }

    @Override
    public RespBody submitSuggestion(SugAddDto sugAddDto) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugAddDto.getUid());
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        suggestion.setStatus(SuggestionStatusEnum.SUBMITTED_AUDIT.getValue());
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }

    @Override
    public RespBody revokeSuggestion(String sugUid) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugUid);
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Date expireAt = DateUtils.addMinutes(suggestion.getRaiseTime(), 5);
        if (expireAt.before(new Date())) {
            body.setMessage("该建议已经提交超过5分钟，无法撤回");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (suggestion.getView()) {
            body.setMessage("该建议已被审核人员查看，无法撤回");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        suggestion.setCanOperate(false);  //撤回后不能再撤回
        suggestion.setStatus(SuggestionStatusEnum.HAS_BEEN_REVOKE.getValue());  //建议状态设置为撤回
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }

    @Override
    public RespBody suggestionDetail(ViewDto viewDto) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(viewDto.getSugUid());
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        //代表查看
        if (StatusEnum.ENABLED.getValue().equals(viewDto.getType())){
            for (SuggestionReply reply : suggestion.getReplies()) {
                reply.setView(true);
            }
        }else if (StatusEnum.DISABLED.getValue().equals(viewDto.getType())) {
            suggestion.setView(true);
        }
        if (viewDto.getChangeDoneView()){
            suggestion.setDoneView(true);
        }
        suggestionRepository.saveAndFlush(suggestion);
        SugDetailVo sugDetailVo = SugDetailVo.convert(suggestion);
        body.setData(sugDetailVo);
        return body;
    }

    @Override
    public RespBody deleteSuggestion(String sugUid) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugUid);
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (!suggestion.getStatus().equals(SuggestionStatusEnum.HAS_BEEN_REVOKE.getValue()) &&
                !suggestion.getStatus().equals(SuggestionStatusEnum.NOT_SUBMITTED.getValue()) &&
                !suggestion.getStatus().equals(SuggestionStatusEnum.AUDIT_FAILURE.getValue())) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议无法删除");
            return body;
        }
        suggestion.setIsDel(true);
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }

    @Override
    public RespBody auditSuggestion(MobileUserDetailsImpl userDetails, SugAuditDto sugAuditDto) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugAuditDto.getUid());
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(sugAuditDto.getLevel(), account.getNpcMembers());
        if (sugAuditDto.getStatus().equals(StatusEnum.ENABLED.getValue())) {  //接受
            suggestion.setStatus(SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue());  //将建议状态设置成“已提交政府”
        } else {
            suggestion.setStatus(SuggestionStatusEnum.AUDIT_FAILURE.getValue());  //将建议状态设置成“审核失败”
        }
        suggestion.setAuditor(npcMember);  //审核人
        suggestion.setAuditReason(sugAuditDto.getReason());
        suggestion.setAuditTime(new Date());
        suggestion.setReason(sugAuditDto.getReason());
        suggestionRepository.saveAndFlush(suggestion);

        //生成一条回复记录
        SuggestionReply suggestionReply = new SuggestionReply();
        suggestionReply.setReply(sugAuditDto.getReason());
        suggestionReply.setSuggestion(suggestion);
        suggestionReply.setReplyer(npcMember);
        suggestionReply.setView(false);
        suggestionReplyRepository.saveAndFlush(suggestionReply);
        return body;
    }

    @Override
    public RespBody npcMemberSug(MobileUserDetailsImpl userDetails, SugPageDto sugPageDto) {
        RespBody body = new RespBody<>();
        int begin = sugPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, sugPageDto.getSize());
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(sugPageDto.getLevel(), account.getNpcMembers());
        scanSuggestion(npcMember);  //扫描建议
        PageVo<SugDetailVo> vo = new PageVo<>(sugPageDto);
        if (npcMember != null) {
            Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                if (!(sugPageDto.getStatus().equals(NpcSugStatusEnum.CAN_SECONDED.getValue()) ||
                        sugPageDto.getStatus().equals(NpcSugStatusEnum.HAS_SECONDED.getValue()) ||
                        sugPageDto.getStatus().equals(NpcSugStatusEnum.SECONDED_COMPLETED.getValue()))) {
                    predicates.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                }
                if (sugPageDto.getStatus() != NpcSugStatusEnum.All.getValue()) {  //分类
                    if (sugPageDto.getStatus().equals(NpcSugStatusEnum.NOT_SUBMIT.getValue())) {  //草稿
                        predicates.add(cb.or(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HAS_BEEN_REVOKE.getValue()),
                                cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.NOT_SUBMITTED.getValue())));  // 0 1
                    } else if (sugPageDto.getStatus().equals(NpcSugStatusEnum.DONE.getValue())) { //已办完
                        predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue()));  // 6
                    } else if (sugPageDto.getStatus().equals(NpcSugStatusEnum.COMPLETED.getValue())) { //已办结
                        predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));  // 7
                    } else if (sugPageDto.getStatus().equals(NpcSugStatusEnum.CAN_SECONDED.getValue())) {  //我能附议的
                        predicates.add(cb.notEqual(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));  //剔除我自己提的
                        predicates.add(cb.equal(root.get("level").as(Byte.class), sugPageDto.getLevel()));  //与我同级别
                        predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));  //与我同区
                        if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue())) {
                            predicates.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));  //与我同镇
                        }
                        predicates.add(cb.or(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()),
                                cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue())));  //待审核或待转办
                        List<Seconded> secondeds = secondedRepository.findByNpcMemberUid(npcMember.getUid());//剔除已经附议的建议
                        if (CollectionUtils.isNotEmpty(secondeds)) {
                            Set<String> sugUids = secondeds.stream().map(seconded -> seconded.getSuggestion().getUid()).collect(Collectors.toSet());
                            predicates.add(root.get("uid").in(sugUids).not());
                        }
                    } else if (sugPageDto.getStatus().equals(NpcSugStatusEnum.HAS_SECONDED.getValue())) {  //我已附议的建议
                        Join<Suggestion, Seconded> join = root.join("secondedSet", JoinType.LEFT);//左连接，把附议表加进来
                        //root.get  表示suggestion的字段
                        predicates.add(cb.notEqual(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));  //建议未办结
                        // join.get  表示seconded的字段
                        predicates.add(cb.equal(join.get("npcMember").get("uid").as(String.class), npcMember.getUid()));  //该代表提出的附议

                    } else if (sugPageDto.getStatus().equals(NpcSugStatusEnum.SECONDED_COMPLETED.getValue())) {  //附议办结的建议
                        Join<Suggestion, Seconded> join = root.join("secondedSet", JoinType.LEFT);//左连接，把附议表加进来
                        //root.get  表示suggestion的字段
                        predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue()));  //建议办结
                        // join.get  表示seconded的字段
                        predicates.add(cb.equal(join.get("npcMember").get("uid").as(String.class), npcMember.getUid()));  //该代表提出的附议
                    } else {  //已提交
                        Predicate or;
                        Predicate submittedAudit = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue());  // 2
                        Predicate submittedGovernment = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue());  // 3
                        Predicate transferredUnit = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.TRANSFERRED_UNIT.getValue());  // 4
                        Predicate handling = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue());  // 5
                        Predicate failure = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.AUDIT_FAILURE.getValue());  // -1
                        if (sugPageDto.getSubStatus().equals((byte)1)){  //全部
                            or = cb.or(submittedAudit, submittedGovernment, transferredUnit, handling, failure);
                        }else if (sugPageDto.getSubStatus().equals((byte)2)){  //未审核
                            or = submittedAudit;
                        }else {  //3 已审核
                            or = cb.or(submittedGovernment, transferredUnit, handling, failure);
                        }
                        predicates.add(or);
                    }
                }
                Predicate[] p = new Predicate[predicates.size()];
                query.where(cb.and(predicates.toArray(p)));
                query.orderBy(cb.desc(root.get("status")), cb.desc(root.get("raiseTime")));
                return query.getRestriction();
            }, page);
            vo.setContent(pageRes.stream().map(SugDetailVo::convert).sorted(Comparator.comparing(SugDetailVo::getMyView)).collect(Collectors.toList()));
            vo.copy(pageRes);
        }
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody auditorSug(MobileUserDetailsImpl userDetails, SugPageDto sugPageDto) {
        RespBody body = new RespBody();
        int begin = sugPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, sugPageDto.getSize());
        PageVo<SugDetailVo> vo = new PageVo<>(sugPageDto);
        Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), sugPageDto.getLevel()));
            //已经撤回的建议不查出来
            predicates.add(cb.notEqual(root.get("status").as(Boolean.class), SuggestionStatusEnum.HAS_BEEN_REVOKE.getValue()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (sugPageDto.getLevel().equals(LevelEnum.TOWN.getValue())) {  //镇建议审核人员
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }

            if (sugPageDto.getStatus() != NpcSugStatusEnum.All.getValue()) {
                if (sugPageDto.getStatus().equals(NpcSugStatusEnum.NOT_SUBMIT.getValue())) {  //1 待审核
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue()));
                } else if (sugPageDto.getStatus().equals(NpcSugStatusEnum.TO_BE_AUDITED.getValue())) {  //2 审核通过
                    Predicate predicate = cb.notEqual(root.get("status").as(Byte.class), SuggestionStatusEnum.NOT_SUBMITTED.getValue());  // 不等于 1 未提交
                    predicate = cb.and(predicate, cb.notEqual(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue()));  // 不等于 2 待审核
                    predicate = cb.and(predicate, cb.notEqual(root.get("status").as(Byte.class), SuggestionStatusEnum.AUDIT_FAILURE.getValue()));  // 不等于 -1 审核失败
                    predicates.add(predicate);
                } else {  // 3 审核失败
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.AUDIT_FAILURE.getValue()));  // -1 审核失败
                }
            }
            Predicate[] p = new Predicate[predicates.size()];
            query.where(cb.and(predicates.toArray(p)));
            query.orderBy(cb.asc(root.get("view")), cb.asc(root.get("status")), cb.desc(root.get("createTime")));
            return query.getRestriction();
        }, page);
        vo.setContent(pageRes.stream().map(SugDetailVo::convert).collect(Collectors.toList()));
        vo.copy(pageRes);
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody acceptResult(MobileUserDetailsImpl userDetails, SugAppraiseDto sugAppraiseDto) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugAppraiseDto.getUid());
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        Result result = null;
        for (Result rs : suggestion.getResults()) {
            if (rs.getAccepted() == null) {
                result = rs;
            }
        }
        result.setReason(sugAppraiseDto.getReason());
        result.setAccepted(true);
        resultRepository.saveAndFlush(result);

        suggestion.setStatus(SuggestionStatusEnum.ACCOMPLISHED.getValue());//修改建议为办结状态
        suggestion.setAccomplishTime(new Date());
        suggestion.setCloseDeadLine(false);//完结之后，不再通知到期
        suggestion.setUrge(false);//完结之后，不再催办
        suggestion.setUrgeLevel(0);//完结之后，清空催办等级
        suggestionRepository.saveAndFlush(suggestion);

        Appraise appraise = new Appraise();
        appraise.setAttitude(sugAppraiseDto.getAttitude());
        appraise.setResult(sugAppraiseDto.getResult());
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(sugAppraiseDto.getLevel(), account.getNpcMembers());
        appraise.setNpcMember(npcMember);
        appraise.setReason(sugAppraiseDto.getReason());
        appraise.setSuggestion(suggestion);
        appraiseRepository.saveAndFlush(appraise);

        return body;
    }

    @Override
    public RespBody refuseResult(SugAppraiseDto sugAppraiseDto) {
        RespBody body = new RespBody();

        Suggestion suggestion = suggestionRepository.findByUid(sugAppraiseDto.getUid());
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        //代表不接受结果，办理单位需要重新办理
        Result result = null;
        for (Result rs : suggestion.getResults()) {
            if (rs.getAccepted() == null) {
                result = rs;
            }
        }
        result.setReason(sugAppraiseDto.getReason());
        result.setAccepted(false);
        resultRepository.saveAndFlush(result);

        ConveySugDto conveySugDto = new ConveySugDto();
        conveySugDto.setUid(suggestion.getUid());

        conveySugDto.setMainUnit(suggestion.getUnit().getUid());
        //办理单位方面转办流程记录
        this.unitDeal(suggestion, conveySugDto, suggestion.getGovernmentUser().getUid());
        suggestion.setDealTimes(suggestion.getDealTimes() + 1);
        suggestion.setStatus(SuggestionStatusEnum.HANDLING.getValue());  //将建议状态设置成“办理中”
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }

    @Override
    public RespBody secondSuggestion(MobileUserDetailsImpl userDetails, SugSecondDto sugSecondDto) {
        RespBody body = new RespBody();

        Suggestion suggestion = suggestionRepository.findByUid(sugSecondDto.getUid());
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(sugSecondDto.getLevel(), account.getNpcMembers());
        Seconded seconded = new Seconded();
        seconded.setAddition(sugSecondDto.getAddition());
        seconded.setNpcMember(npcMember);
        seconded.setSecondedTime(new Date());
        seconded.setSuggestion(suggestion);
        secondedRepository.saveAndFlush(seconded);
        return body;
    }

    @Override
    public RespBody urgeSuggestion(MobileUserDetailsImpl userDetails, String sugUid) {
        RespBody body = new RespBody();

        Account account = accountRepository.findByUid(userDetails.getUid());
        Suggestion suggestion = suggestionRepository.findByUid(sugUid);

        if (suggestion == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议不存在");
            return body;
        }

        if (suggestion.getStatus() != 4 && suggestion.getStatus() != 5) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该状态下建议不能催办");
            return body;
        }

//        int urgeFre = suggestionSettingRepository.findUrgeFre();//催办周期
        int urgeFre = 7;  //暂时写死为7天

        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.DATE, -urgeFre);
        Date urgeDate = beforeTime.getTime();  //当前时间的 催办周期天数之前

        Boolean canUrge = false;
        int urgeScore = 0;
        if (suggestion.getUrge()) {//如果该建议已经催办过
            for (Urge urge : suggestion.getUrges()) {
                urgeScore += urge.getScore();
                if (urge.getAccount().getUid().equals(account.getUid()) && urge.getCreateTime().before(urgeDate)) {//距离上一次催办已经过去了设置的时间了，那么才可以进行下一次催办
                    canUrge = true;
                }
            }
        } else {
            canUrge = true;
        }
        if (canUrge) {
            Urge urge = new Urge();
            urge.setType((byte)1);  //1 代表催办
            urge.setCreateTime(new Date());
            urge.setAccount(account);
            urge.setScore(1);         //代表催办一次1分，联工委催办一次4分，政府催办一次16分
            suggestion.setUrge(true);
            suggestion.setUrgeLevel(urgeScore + urge.getScore());//重新计算催办等级
            urge.setSuggestion(suggestion);
            urgeRepository.saveAndFlush(urge);
        } else {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage(urgeFre + "天内请勿重复催办");
            return body;
        }
        body.setMessage("催办成功");
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
        suggestionImage.setTransUid(suggestion.getTransUid());
        suggestionImage.setUrl(url);
        suggestionImage.setSuggestion(suggestion);
        suggestionImageRepository.saveAndFlush(suggestionImage);
    }

    //扫描我的所有建议，判断哪些可以撤回
    private void scanSuggestion(NpcMember member) {
        List<Suggestion> suggestions = suggestionRepository.findByRaiserUid(member.getUid());
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.MINUTE, -5);// 5分钟之前的时间
        Date beforeDate = beforeTime.getTime();
        for (Suggestion suggestion : suggestions) {
            //已被查看或者已经超过5分钟不可撤回
            if (suggestion.getView() || !suggestion.getRaiseTime().after(beforeDate)) {
                suggestion.setCanOperate(false);
            }
        }
        suggestionRepository.saveAll(suggestions);
    }

    /**
     * 代表拒绝结果后办理单位方面重新办理流程记录
     *
     * @param suggestion
     * @param conveySugDto
     * @param userUid
     */
    private void unitDeal(Suggestion suggestion, ConveySugDto conveySugDto, String userUid) {
        Set<UnitSuggestion> unitSuggestionList = Sets.newHashSet();
        if (conveySugDto.getMainUnit() != null) {
            //主办单位
            UnitSuggestion unitSuggestion = new UnitSuggestion();
            Unit unit = unitRepository.findByUid(conveySugDto.getUid());
            unitSuggestion.setUnit(unit);//办理单位
            unitSuggestion.setSuggestion(suggestion);//建议
            unitSuggestion.setType((byte) 1);//主办单位
            GovernmentUser governmentUser = governmentUserRepository.findByUid(userUid);
            unitSuggestion.setGovernmentUser(governmentUser);  //转交的政府人员
            unitSuggestion.setReceiveTime(new Date());
            unitSuggestion.setDealTimes(suggestion.getDealTimes() + 1);
            unitSuggestion.setUnitUser(this.getUnitUser(suggestion, conveySugDto.getMainUnit()));
            unitSuggestionList.add(unitSuggestion);
        }
        //协办单位
        if (conveySugDto.getSponsorUnits() != null) {
            for (String sponsorUnit : conveySugDto.getSponsorUnits()) {
                UnitSuggestion sponsorSuggestion = new UnitSuggestion();
                Unit unit = unitRepository.findByUid(sponsorUnit);
                sponsorSuggestion.setUnit(unit);//办理单位
                sponsorSuggestion.setSuggestion(suggestion);//建议
                sponsorSuggestion.setType((byte) 2);//协办单位
                GovernmentUser governmentUser = governmentUserRepository.findByUid(userUid);
                sponsorSuggestion.setGovernmentUser(governmentUser);
                sponsorSuggestion.setReceiveTime(new Date());//收到时间
                sponsorSuggestion.setDealTimes(suggestion.getDealTimes() + 1);
                sponsorSuggestion.setUnitUser(this.getUnitUser(suggestion, sponsorUnit));
                unitSuggestionList.add(sponsorSuggestion);
            }
        }
        if (CollectionUtils.isNotEmpty(unitSuggestionList)) {
            unitSuggestionRepository.saveAll(unitSuggestionList);
        }
    }

    private UnitUser getUnitUser(Suggestion suggestion, String unitUid) {
        UnitUser unitUser = null;
        for (UnitSuggestion unitSuggestion : suggestion.getUnitSuggestions()) {
            if (suggestion.getDealTimes().equals(unitSuggestion.getDealTimes()) && unitSuggestion.getUnit().getUid().equals(unitUid)) {
                unitUser = unitSuggestion.getUnitUser();
                break;
            }
        }
        return unitUser;
    }
}
