package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.*;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.GovernmentUserRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.member_house.*;
import com.cdkhd.npc.repository.suggestion_deal.*;
import com.cdkhd.npc.service.NpcSuggestionService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.NumbericRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.deepoove.poi.data.builder.StyleBuilder;
import com.deepoove.poi.data.style.Style;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
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

    private final PerformanceTypeRepository performanceTypeRepository;

    private final PerformanceImageRepository performanceImageRepository;

    private final PerformanceRepository performanceRepository;

    private final SuggestionSettingRepository suggestionSettingRepository
            ;
    private final NpcMemberRepository npcMemberRepository;

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
                                    GovernmentUserRepository governmentUserRepository, SecondedRepository secondedRepository, UrgeRepository urgeRepository, PerformanceTypeRepository performanceTypeRepository, PerformanceImageRepository performanceImageRepository, PerformanceRepository performanceRepository, SuggestionSettingRepository suggestionSettingRepository, NpcMemberRepository npcMemberRepository) {
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
        this.performanceTypeRepository = performanceTypeRepository;
        this.performanceImageRepository = performanceImageRepository;
        this.performanceRepository = performanceRepository;
        this.suggestionSettingRepository = suggestionSettingRepository;
        this.npcMemberRepository = npcMemberRepository;
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
    public RespBody suggestionDetail(MobileUserDetailsImpl userDetails, ViewDto viewDto) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(viewDto.getSugUid());
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(viewDto.getLevel(), account.getNpcMembers());

        //如果是代表查看，查看审核人回复时候消除未读
        if (StatusEnum.ENABLED.getValue().equals(viewDto.getType())) {
            for (SuggestionReply reply : suggestion.getReplies()) {
                reply.setView(true);
            }
            suggestion.setNpcView(true);
        } else if (StatusEnum.DISABLED.getValue().equals(viewDto.getType())) {  //如果是审核人查看，查看新提交的数据的时候消除未读
            suggestion.setView(true);
        }
        //代表消去办理完成的未读
        if (viewDto.getChangeDoneView()) {
            suggestion.setDoneView(true);
        }

        //代表消去附议办结的未读
        if (viewDto.getChangeSecondView()) {
            Set<Seconded> secondedSet = suggestion.getSecondedSet();
            for (Seconded seconded : secondedSet) {
                if (seconded.getNpcMember().getUid().equals(npcMember.getUid())) {
                    seconded.setView(true);
                    secondedRepository.saveAndFlush(seconded);
                    break;
                }
            }
        }
        suggestionRepository.saveAndFlush(suggestion);

        SugDetailVo sugDetailVo = SugDetailVo.convert(suggestion);

        //评价
        if (suggestion.getAppraise() != null) {
            sugDetailVo.setAppraiseVo(AppraiseVo.convert(suggestion.getAppraise()));
        }

        //办理结果
        if (suggestion.getResult() != null) {
            sugDetailVo.setResultVo(ResultVo.convert(suggestion.getResult()));
        }

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
        suggestion.setGovView(false);  //审核过后将govView字段改成false，便于政府未读查询
        suggestion.setNpcView(false);  //审核过后将npcView字段改成false，便于代表审核通过的未读查询
        suggestionRepository.saveAndFlush(suggestion);

        if (sugAuditDto.getStatus().equals(StatusEnum.ENABLED)){  //审核通过
            //生成一条履职
            AddPerformanceDto addPerformanceDto = new AddPerformanceDto();
            addPerformanceDto.setContent(suggestion.getContent());
            addPerformanceDto.setLevel(sugAuditDto.getLevel());
            addPerformanceDto.setPerformanceType(PerformanceTypeEnum.SUGGESTION.getValue());
            addPerformanceDto.setTitle(suggestion.getTitle());
            addPerformanceDto.setWorkAt(suggestion.getRaiseTime());
            addPerformanceDto.setUid(suggestion.getRaiser().getUid());
            addPerformanceDto.setReason(sugAuditDto.getReason());
            Set<SuggestionImage> suggestionImages = suggestion.getSuggestionImages();
            this.addPerformanceFormSug(userDetails, addPerformanceDto, suggestionImages);
        }

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
                        if (sugPageDto.getSubStatus().equals((byte) 1)) {  //全部
                            or = cb.or(submittedAudit, submittedGovernment, transferredUnit, handling, failure);
                        } else if (sugPageDto.getSubStatus().equals((byte) 2)) {  //未审核
                            or = submittedAudit;
                        } else {  //3 已审核
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
            Set<Suggestion> suggestions = new HashSet<>(pageRes.getContent());
            List<SugDetailVo> sugDetailVos = suggestions.stream().map(SugDetailVo::convert).sorted((e1, e2) -> e2.getRaiseTime().compareTo(e1.getRaiseTime())).collect(Collectors.toList());

            for (SugDetailVo sugDetailVo : sugDetailVos) {
                for (Suggestion suggestion : suggestions) {
                    if (sugDetailVo.getUid().equals(suggestion.getUid())) {
                        for (Seconded seconded : suggestion.getSecondedSet()) {
                            if (seconded.getNpcMember().getUid().equals(npcMember.getUid()) && seconded.getView() == true) {
                                sugDetailVo.setSecondDoneView(true);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            vo.setContent(sugDetailVos);
            vo.copy(pageRes);
        }
        body.setData(vo);
        return body;
    }

    @Override
    public RespBody auditorSug(MobileUserDetailsImpl userDetails, SugPageDto sugPageDto) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(sugPageDto.getLevel(), account.getNpcMembers());
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
                    predicate = cb.and(predicate, cb.equal(root.get("auditor").get("uid").as(String.class), npcMember.getUid()));  // 我自己审核的
                    predicates.add(predicate);
                } else {  // 3 审核失败
                    Predicate predicate = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.AUDIT_FAILURE.getValue());  // -1 审核失败
                    predicate = cb.and(predicate, cb.equal(root.get("auditor").get("uid").as(String.class), npcMember.getUid()));  // 我自己审核的
                    predicates.add(predicate);
                }
            }
            Predicate[] p = new Predicate[predicates.size()];
            query.where(cb.and(predicates.toArray(p)));
//            query.orderBy(cb.asc(root.get("view")), cb.asc(root.get("status")), cb.desc(root.get("createTime")));
            return query.getRestriction();
        }, page);
        List<SugDetailVo> sugDetailVos = pageRes.stream().map(SugDetailVo::convert).sorted((e1, e2) -> e2.getRaiseTime().compareTo(e1.getRaiseTime())).collect(Collectors.toList());
        vo.setContent(sugDetailVos);
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

        Result result = suggestion.getResult();
        result.setReason(sugAppraiseDto.getReason());
        result.setAccepted(true);
        resultRepository.saveAndFlush(result);

        suggestion.setStatus(SuggestionStatusEnum.ACCOMPLISHED.getValue());//修改建议为办结状态
        suggestion.setAccomplishTime(new Date());
        suggestion.setCloseDeadLine(false);//完结之后，不再通知到期
        suggestion.setUrge(false);//完结之后，不再催办
        suggestion.setUrgeLevel(0);//完结之后，清空催办等级
        suggestion.setGovView(false);  //评价过后将govView字段改成false，便于政府未读查询
        suggestionRepository.saveAndFlush(suggestion);

        //评价后要将unitSuggestion的unitView设置为false，便于办理单位未读查询
        Set<UnitSuggestion> unitSuggestions = suggestion.getUnitSuggestions();
        for (UnitSuggestion unitSuggestion : unitSuggestions) {
            unitSuggestion.setUnitView(false);
        }
        unitSuggestionRepository.saveAll(unitSuggestions);

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
        Result result = suggestion.getResult();
        result.setReason(sugAppraiseDto.getReason());
        result.setAccepted(false);
        result.setSuggestion(null);
        resultRepository.saveAndFlush(result);

        ConveySugDto conveySugDto = new ConveySugDto();
        conveySugDto.setUid(suggestion.getUid());

        conveySugDto.setMainUnit(suggestion.getUnit().getUid());

        conveySugDto.setSponsorUnits(suggestion.getUnitSuggestions().stream()
                .filter(unitSuggestion -> unitSuggestion.getType().equals(UnitTypeEnum.CO_UNIT.getValue())
                        && unitSuggestion.getDealTimes().equals(suggestion.getDealTimes()))
                .map(unitSuggestion -> unitSuggestion.getUnit().getUid()).collect(Collectors.toList()));

        suggestion.setDealTimes(suggestion.getDealTimes() + 1);
        suggestion.setStatus(SuggestionStatusEnum.HANDLING.getValue());  //将建议状态设置成“办理中”
        suggestionRepository.saveAndFlush(suggestion);
        //办理单位方面转办流程记录
        this.unitDeal(suggestion, conveySugDto, suggestion.getGovernmentUser().getUid());

        //拒绝后要将unitSuggestion的unitView设置为false，便于办理单位未读查询
        Set<UnitSuggestion> unitSuggestions = suggestion.getUnitSuggestions();
        for (UnitSuggestion unitSuggestion : unitSuggestions) {
            unitSuggestion.setUnitView(false);
        }
        unitSuggestionRepository.saveAll(unitSuggestions);

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
    public RespBody urgeSuggestion(MobileUserDetailsImpl userDetails, Byte level, String sugUid) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        Suggestion suggestion = suggestionRepository.findByUid(sugUid);
        if (suggestion == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该建议不存在");
            return body;
        }
        if (suggestion.getStatus() != 4 && suggestion.getStatus() != 5) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该状态下建议不能催办");
            return body;
        }
        SuggestionSetting suggestionSetting = null;//催办周期
        if (level.equals(LevelEnum.TOWN.getValue())) {
            suggestionSetting = suggestionSettingRepository.findByLevelAndTownUid(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid());
        }else if (level.equals(LevelEnum.AREA.getValue())){
            suggestionSetting = suggestionSettingRepository.findByLevelAndAreaUid(LevelEnum.AREA.getValue(),userDetails.getArea().getUid());
        }
        if (suggestionSetting == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("系统错误，请联系管理员");
            return body;
        }
        int urgeFre = suggestionSetting.getUrgeFre();
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.DATE, -urgeFre);
        Date urgeDate = beforeTime.getTime();  //当前时间的 催办周期天数之前
        List<Urge> urges = urgeRepository.findBySuggestionUidAndType(sugUid,UrgeScoreEnum.NPC_MEMBER.getType());
        for (Urge urge : urges) {
            if (urge.getCreateTime().after(urgeDate)){
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("该建议已经催办！ " +urgeFre+"天之内，不能再次催办！");
                return body;
            }
        }
        Urge urge = new Urge();
        urge.setType((byte) 1);  //1 代表催办
        urge.setCreateTime(new Date());
        urge.setAccount(account);
        urge.setScore(1);         //代表催办一次1分，联工委催办一次2分，政府催办一次4分
        suggestion.setUrge(true);
        suggestion.setUrgeLevel(1 + urge.getScore());//重新计算催办等级
        urge.setSuggestion(suggestion);
        urgeRepository.saveAndFlush(urge);
        suggestionRepository.saveAndFlush(suggestion);
        body.setMessage("催办成功");
        return body;
    }

    @Override
    public RespBody handleProcessDetail(String sugUid, Byte type) {  //查询办理单位的办理流程
        //type = 1表示查询主办单位  type = 2表示查询协办单位
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugUid);
        if (suggestion == null) {
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        //每个单位的办理记录
        List<UnitProcessVo> unitProcessVos = new ArrayList<>();

        //每个单位收到的该建议的办理记录
        Map<Unit, List<UnitSuggestion>> map = new HashMap<>();

        //该建议对应的所有办理记录（可能一个办理单位有多个办理记录）
        Set<UnitSuggestion> unitSuggestions = suggestion.getUnitSuggestions();

        if (unitSuggestions != null) {
            for (UnitSuggestion unitSuggestion : unitSuggestions) {
                if (unitSuggestion.getType().equals(type)) {
                    Unit unit = unitSuggestion.getUnit();  //这条办理记录对应的办理单位
                    List<UnitSuggestion> unitSuggestionList;  //该办理单位对应的该条建议的办理记录
                    if (map.get(unit) == null) {
                        unitSuggestionList = new ArrayList<>();
                    } else {
                        unitSuggestionList = map.get(unit);
                    }
                    unitSuggestionList.add(unitSuggestion);
                    map.put(unit, unitSuggestionList);
                }
            }
        }

        for (Unit unit : map.keySet()) {
            UnitProcessVo unitProcessVo = new UnitProcessVo();
            unitProcessVo.setUnitName(unit.getName());
            List<UnitSuggestion> unitSuggestionList = map.get(unit);
            List<UnitSugDetailVo> unitSugDetailVos = new ArrayList<>();
            for (UnitSuggestion unitSuggestion : unitSuggestionList) {
                UnitSugDetailVo unitSugDetailVo = UnitSugDetailVo.convert(unitSuggestion);
                unitSugDetailVos.add(unitSugDetailVo);
            }
            unitProcessVo.setUnitSugDetailVos(unitSugDetailVos);
            unitProcessVos.add(unitProcessVo);
        }

        body.setData(unitProcessVos);
        return body;
    }

    @Override
    public String detailDoc(HttpServletRequest req, HttpServletResponse res, String sugUid) {
        String result = "成功";
        Suggestion suggestion = suggestionRepository.findByUid(sugUid);
        if (suggestion == null) {
            return "未找到该建议";
        }
        Map<String, Object> datas = new HashMap<>();
        datas.put("name", suggestion.getRaiser().getName());
        datas.put("gender", suggestion.getRaiser().getGender() == 1 ? "男" : "女");
        datas.put("mobile", suggestion.getRaiser().getMobile());
        datas.put("title", suggestion.getTitle());
        datas.put("content", suggestion.getContent());
        Set<SuggestionReply> replySet = suggestion.getReplies();
        if (CollectionUtils.isEmpty(replySet)) {
            datas.put("returnInfo", "暂无回复");
        } else {
            List<TextRenderData> renderDataList = Lists.newArrayList();
            Style style = StyleBuilder.newBuilder().buildFontSize(16).build();
            for (SuggestionReply suggestionReply : replySet) {
                suggestionReply.setView(true);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String repInfo = simpleDateFormat.format(suggestionReply.getCreateTime()) + " : " + suggestionReply.getReply();
                renderDataList.add(new TextRenderData(repInfo, style));
            }
            datas.put("returnInfo", new NumbericRenderData(NumbericRenderData.FMT_DECIMAL, style, renderDataList));
            suggestionReplyRepository.saveAll(replySet);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(suggestion.getRaiseTime());
        datas.put("year", calendar.get(Calendar.YEAR));
        datas.put("month", calendar.get(Calendar.MONTH) + 1);
        datas.put("day", calendar.get(Calendar.DATE));
        XWPFTemplate template = XWPFTemplate.compile("template/suggest_word_template.docx").render(datas);
        FileOutputStream out = null;
        String parentPath = String.format("static/public/suggest/%s", suggestion.getRaiser().getUid());
        File beforeFile = new File("template/suggest_word_template.docx");
        File bgFile = new File(parentPath, "suggestion.docx");
        try {
            if (!bgFile.exists()) {
                FileUtils.copyFile(beforeFile, bgFile);
            }
            result = bgFile.getPath().replace("static", "");
            out = new FileOutputStream(bgFile.getPath());
            template.write(out);
            out.flush();
        } catch (FileNotFoundException e) {
            result = "失败";
            e.printStackTrace();
        } catch (IOException e) {
            result = "失败";
            e.printStackTrace();
        } finally {
            try {
                out.close();
                template.close();
            } catch (IOException e) {
                result = "失败";
                e.printStackTrace();
            }
            return result;
        }
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
            Unit unit = unitRepository.findByUid(conveySugDto.getMainUnit());
            unitSuggestion.setUnit(unit);//办理单位
            unitSuggestion.setSuggestion(suggestion);//建议
            unitSuggestion.setType(UnitTypeEnum.MAIN_UNIT.getValue());//主办单位
            GovernmentUser governmentUser = governmentUserRepository.findByUid(userUid);
            unitSuggestion.setGovernmentUser(governmentUser);  //转交的政府人员
            unitSuggestion.setReceiveTime(new Date());
            unitSuggestion.setAcceptTime(new Date());
            unitSuggestion.setDealTimes(suggestion.getDealTimes());
            unitSuggestion.setExpectDate(suggestion.getExpectDate());
            unitSuggestion.setUnitView(false);

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
                sponsorSuggestion.setType(UnitTypeEnum.CO_UNIT.getValue());//协办单位
                GovernmentUser governmentUser = governmentUserRepository.findByUid(userUid);
                sponsorSuggestion.setGovernmentUser(governmentUser);
                sponsorSuggestion.setReceiveTime(new Date());//收到时间
                sponsorSuggestion.setAcceptTime(new Date());//接受时间
                sponsorSuggestion.setDealTimes(suggestion.getDealTimes());

                sponsorSuggestion.setUnitUser(this.getUnitUser(suggestion, sponsorUnit));

                sponsorSuggestion.setExpectDate(suggestion.getExpectDate());
                sponsorSuggestion.setUnitView(false);
                unitSuggestionList.add(sponsorSuggestion);
            }
        }
        if (CollectionUtils.isNotEmpty(unitSuggestionList)) {
            unitSuggestionRepository.saveAll(unitSuggestionList);
        }
    }

    private UnitUser getUnitUser(Suggestion suggestion, String unitUid) {  //unitUid：办理单位的
        UnitUser unitUser = null;
        Set<UnitSuggestion> unitSuggestions = suggestion.getUnitSuggestions();
        if (unitSuggestions != null) {
            for (UnitSuggestion unitSuggestion : unitSuggestions) {
                if (suggestion.getDealTimes().equals(unitSuggestion.getDealTimes() + 1) && unitSuggestion.getUnit().getUid().equals(unitUid)) {
                    unitUser = unitSuggestion.getUnitUser();
                    break;
                }
            }
        }

        return unitUser;
    }


    public RespBody addPerformanceFormSug(MobileUserDetailsImpl userDetails, AddPerformanceDto addPerformanceDto, Set<SuggestionImage> suggestionImages) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(addPerformanceDto.getLevel(), account.getNpcMembers());
        Performance performance = new Performance();
        performance.setArea(npcMember.getArea());
        performance.setLevel(addPerformanceDto.getLevel());
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
            performanceImage.setUrl(suggestionImage.getUrl());
            performanceImage.setPerformance(performance);
            performanceImages.add(performanceImage);
        }
        performanceImageRepository.saveAll(performanceImages);
        performance.setPerformanceImages(performanceImages);
        performanceRepository.saveAndFlush(performance);

        return body;
    }
}
