package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.SuggestionVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionImageRepository;
import com.cdkhd.npc.repository.member_house.SuggestionReplyRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.PerformanceService;
import com.cdkhd.npc.service.PushMessageService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SuggestionServiceImpl implements SuggestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestionServiceImpl.class);

    private final SuggestionRepository suggestionRepository;

    private final AccountRepository accountRepository;

    private final SuggestionImageRepository suggestionImageRepository;

    private final SuggestionBusinessRepository suggestionBusinessRepository;

    private final SuggestionReplyRepository suggestionReplyRepository;

    private final PerformanceService performanceService;

    private final NpcMemberRepository npcMemberRepository;

    private final PushMessageService pushMessageService;

    private final Environment env;

    @Autowired
    public SuggestionServiceImpl(SuggestionRepository suggestionRepository, AccountRepository accountRepository, SuggestionImageRepository suggestionImageRepository, NpcMemberRepository npcMemberRepository, SuggestionBusinessRepository suggestionBusinessRepository, SuggestionReplyRepository suggestionReplyRepository, PerformanceService performanceService, NpcMemberRepository npcMemberRepository1, PushMessageService pushMessageService, Environment env) {
        this.suggestionRepository = suggestionRepository;
        this.accountRepository = accountRepository;
        this.suggestionImageRepository = suggestionImageRepository;
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.suggestionReplyRepository = suggestionReplyRepository;
        this.performanceService = performanceService;
        this.npcMemberRepository = npcMemberRepository1;
        this.pushMessageService = pushMessageService;
        this.env = env;
    }

    /**
     * ??????????????????
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody sugBusList(MobileUserDetailsImpl userDetails, SuggestionBusinessDto dto) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> sb = Lists.newArrayList();
        //??????????????????????????????????????????????????????
        if (dto.getLevel().equals(LevelEnum.AREA.getValue()) || (dto.getLevel().equals(LevelEnum.TOWN.getValue()) && userDetails.getTown().getType().equals(LevelEnum.AREA.getValue()))){
            sb = suggestionBusinessRepository.findByLevelAndAreaUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.AREA.getValue(),userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }else if (dto.getLevel().equals(LevelEnum.TOWN.getValue())) {
            sb = suggestionBusinessRepository.findByLevelAndTownUidAndStatusAndIsDelFalseOrderBySequenceAsc(LevelEnum.TOWN.getValue(),userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        }
        List<CommonVo> commonVos = sb.stream().map(sugBus -> CommonVo.convert(sugBus.getUid(), sugBus.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }

    @Override
    public RespBody npcMemberSug(MobileUserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody<PageVo<SuggestionVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize());
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(dto.getLevel(), account.getNpcMembers());
        scanSuggestion(npcMember);  //????????????
        PageVo<SuggestionVo> vo = new PageVo<>(dto);
        if (npcMember != null) {
            Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                predicates.add(cb.equal(root.get("level").as(Byte.class), npcMember.getLevel()));
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
                if (dto.getLevel().equals(LevelEnum.TOWN.getValue())){
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
                }
                predicates.add(cb.notEqual(root.get("status").as(Byte.class), SuggestionStatusEnum.NOT_SUBMITTED.getValue()));
                if (dto.getStatus() != null){
                    if (dto.getStatus().equals(MobileSugStatusEnum.TO_BE_AUDITED.getValue())){  //?????????
                        predicates.add(cb.equal(root.get("status").as(Byte.class), MobileSugStatusEnum.TO_BE_AUDITED.getValue()));
                    }else if (dto.getStatus().equals(MobileSugStatusEnum.HAS_BEEN_AUDITED.getValue())){  //?????????
                        predicates.add(cb.notEqual(root.get("status").as(Byte.class), MobileSugStatusEnum.TO_BE_AUDITED.getValue()));
                        predicates.add(cb.notEqual(root.get("status").as(Byte.class), SuggestionStatusEnum.HAS_BEEN_REVOKE.getValue()));
                    }
                }
                Predicate[] p = new Predicate[predicates.size()];
                query.where(cb.and(predicates.toArray(p)));
                query.orderBy(cb.desc(root.get("status")),cb.desc(root.get("createTime")));
                return query.getRestriction();
            }, page);
            vo.setContent(pageRes.stream().map(SuggestionVo::convert).sorted(Comparator.comparing(SuggestionVo::getMyView)).collect(Collectors.toList()));
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

        //?????????????????????????????????????????????????????????
        if (npcMember == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????/?????????????????????????????????");
            return body;
        }

        Suggestion suggestion;
        //????????????uid????????????????????????
        if (StringUtils.isEmpty(dto.getUid())) {
            //??????uid??????????????????
            //?????????????????????????????????
            suggestion = suggestionRepository.findByTransUid(dto.getTransUid());
        } else {
            //???uid??????????????????
            //?????????????????????????????????
            suggestion = suggestionRepository.findByUidAndTransUid(dto.getUid(),dto.getTransUid());
            if (suggestion == null){  //???????????????????????????????????????????????????
                suggestion = suggestionRepository.findByUid(dto.getUid());
                Set<SuggestionImage> images = suggestion.getSuggestionImages();
                suggestionImageRepository.deleteAll(images);
                suggestion.setTransUid(dto.getTransUid());
            }
        }
        if (suggestion == null) { //????????????????????????????????????????????????
            suggestion = new Suggestion();
            suggestion.setLevel(dto.getLevel());
            suggestion.setArea(npcMember.getArea());
            suggestion.setTown(npcMember.getTown());
            suggestion.setRaiser(npcMember);
            suggestion.setLeader(npcMember);
            suggestion.setTransUid(dto.getTransUid());

            //????????????????????????????????????????????????????????????

            JSONObject suggestionMsg = new JSONObject();
            List<NpcMember> npcMembers;
            if (dto.getLevel().equals(LevelEnum.TOWN.getValue())){  //???????????????????????????????????????????????????????????????
                npcMembers = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(userDetails.getTown().getUid(), LevelEnum.TOWN.getValue());
            }else {  //???????????????????????????????????????????????????????????????
                npcMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(), LevelEnum.AREA.getValue());
            }
            for (NpcMember npcMember1 : npcMembers){
                boolean flag = false;
                Set<NpcMemberRole> npcMemberRoles = npcMember1.getNpcMemberRoles();
                for (NpcMemberRole npcMemberRole : npcMemberRoles){
                    if (npcMemberRole.getKeyword().equals(NpcMemberRoleEnum.SUGGESTION_RECEIVER.getKeyword())){
                        flag = true;
                        break;
                    }
                }
                Account account1 = npcMember1.getAccount();
                if (flag && account1 != null){
                    suggestionMsg.put("subtitle","????????????????????????????????????????????????????????????");
                    suggestionMsg.put("accountName",npcMember.getName());
                    suggestionMsg.put("mobile",npcMember.getMobile());
                    suggestionMsg.put("content",dto.getContent());
                    suggestionMsg.put("remarkInfo","?????????????????????????????????");
                    pushMessageService.pushMsg(account1, MsgTypeEnum.NEW_OPINION_OR_SUGGESTION.ordinal(),suggestionMsg);
                }
            }
        }
        suggestion.setTitle(dto.getTitle());
        suggestion.setContent(dto.getContent());
        suggestion.setRaiser(npcMember);
        suggestion.setRaiseTime(new Date());
        suggestion.setView(false);
        suggestion.setCanOperate(true);
        suggestion.setStatus(SuggestionStatusEnum.SUBMITTED_AUDIT.getValue());  //??????????????????????????????????????????
        SuggestionBusiness suggestionBusiness = suggestionBusinessRepository.findByUid(dto.getBusiness());
        if (suggestionBusiness == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        suggestion.setSuggestionBusiness(suggestionBusiness);
        suggestionRepository.saveAndFlush(suggestion);

        if (dto.getImage() != null) {  //??????????????????????????????
            this.saveCover(dto.getImage(), suggestion);
        }
        return body;
    }

    public void saveCover(MultipartFile cover, Suggestion suggestion) {
        //???????????????????????????
        String url = ImageUploadUtil.saveImage("suggestionImage", cover, 500, 500);
        if (url.equals("error")) {
            LOGGER.error("?????????????????????????????????");
        }
        //????????????????????????
        SuggestionImage suggestionImage = new SuggestionImage();
        suggestionImage.setUrl(url);
        suggestionImage.setTransUid(suggestion.getTransUid());
        suggestionImage.setSuggestion(suggestion);
        suggestionImageRepository.saveAndFlush(suggestionImage);
    }

    @Override
    public RespBody deleteSuggestion(String uid) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(uid)) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("????????????????????????");
            return body;
        }
        Suggestion suggestion = suggestionRepository.findByUid(uid);
        if (suggestion == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????");
            return body;
        }
        if (suggestion.getStatus() != null){
            if (!suggestion.getStatus().equals(StatusEnum.FAILURE.getValue()) && !suggestion.getStatus().equals(StatusEnum.REVOKE.getValue())){
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("??????????????????");
                return body;
            }
        }
        suggestion.setIsDel(true);  //????????????
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }

    @Override
    public RespBody suggestionDetail(ViewDto viewDto) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(viewDto.getUid());
        if (suggestion == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("?????????????????????");
            return body;
        }
        //??????????????????????????????????????????????????????
        if (null != viewDto.getType() && viewDto.getType().equals(StatusEnum.ENABLED.getValue())){
            for (SuggestionReply reply : suggestion.getReplies()) {
                reply.setView(true);
            }
            suggestionRepository.saveAndFlush(suggestion);
        }else if (null != viewDto.getType() && viewDto.getType().equals(StatusEnum.DISABLED.getValue())) {
            suggestion.setView(true);
            suggestionRepository.saveAndFlush(suggestion);
        }
        SuggestionVo suggestionVo = SuggestionVo.convert(suggestion);
        body.setData(suggestionVo);
        return body;
    }

    @Override
    public RespBody audit(MobileUserDetailsImpl userDetails, SuggestionAuditDto suggestionAuditDto) {
        //????????????????????????????????????????????????????????????
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(suggestionAuditDto.getUid());
        if (suggestion == null) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????");
            return body;
        }

        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(suggestionAuditDto.getLevel(), account.getNpcMembers());
        if (suggestionAuditDto.getStatus().equals(StatusEnum.ENABLED.getValue())){
            suggestion.setStatus(SuggestionStatusEnum.SELF_HANDLE.getValue());  //??????????????????????????????????????????
        }else {
            suggestion.setStatus(SuggestionStatusEnum.AUDIT_FAILURE.getValue());  //??????????????????????????????????????????
        }
        suggestion.setAuditReason(suggestionAuditDto.getReason());
        suggestion.setAuditTime(new Date());
        suggestion.setAuditor(npcMember);
        suggestion.setReason(suggestionAuditDto.getReason());
        suggestionRepository.saveAndFlush(suggestion);

        SuggestionReply suggestionReply = new SuggestionReply();
        suggestionReply.setReply(suggestionAuditDto.getReason());
        suggestionReply.setSuggestion(suggestion);
        suggestionReply.setReplyer(npcMember);
        suggestionReply.setView(false);
        suggestionReplyRepository.saveAndFlush(suggestionReply);

        if (suggestionAuditDto.getStatus().equals((byte)1)){  //????????????
            //??????????????????
            AddPerformanceDto addPerformanceDto = new AddPerformanceDto();
            addPerformanceDto.setContent(suggestion.getContent());
            addPerformanceDto.setLevel(suggestionAuditDto.getLevel());
            addPerformanceDto.setPerformanceType(PerformanceTypeEnum.SUGGESTION.getValue());
            addPerformanceDto.setTitle(suggestion.getTitle());
            addPerformanceDto.setWorkAt(suggestion.getRaiseTime());
            addPerformanceDto.setUid(suggestion.getRaiser().getUid());
            addPerformanceDto.setReason(suggestionAuditDto.getReason());
            Set<SuggestionImage> suggestionImages = suggestion.getSuggestionImages();
            performanceService.addPerformanceFormSug(userDetails, addPerformanceDto, suggestionImages);
        }
        Account raiser = suggestion.getRaiser().getAccount();//????????????????????????????????????????????????
        if (raiser != null){
            JSONObject suggestionMsg = new JSONObject();
            suggestionMsg.put("subtitle","????????????????????????????????????????????????????????????");
            suggestionMsg.put("title",suggestion.getTitle());
            suggestionMsg.put("content",suggestionReply.getReply());
            suggestionMsg.put("remarkInfo","????????????"+ suggestion.getAuditor().getName()+" <??????????????????>");
            pushMessageService.pushMsg(raiser, MsgTypeEnum.FEEDBACK.ordinal(),suggestionMsg);
        }
        return body;
    }

    @Override
    public RespBody suggestionRevoke(String uid) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(uid);
        if (suggestion == null) {
            body.setStatus(HttpStatus.NOT_FOUND);
            body.setMessage("???????????????");
            return body;
        }
//        String time = env.getProperty("code.overdueTime");  //????????????????????????????????????????????????
//        if (StringUtils.isNotBlank(time)) {
//            timeout = Integer.parseInt(time);
//        }
        Date expireAt = DateUtils.addMinutes(suggestion.getRaiseTime(), 5);  //????????????

        Boolean view = suggestion.getView();
        if (expireAt.before(new Date()) || view) {
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("?????????5???????????????????????????????????????????????????");
            return body;
        }
        suggestion.setStatus(StatusEnum.REVOKE.getValue());  //??????????????????????????????
        suggestion.setCanOperate(false);  //????????????????????????
        suggestionRepository.saveAndFlush(suggestion);
        return body;
    }



    @Override
    public RespBody auditorSug(MobileUserDetailsImpl userDetails, SuggestionPageDto dto) {
        RespBody<PageVo<SuggestionVo>> body = new RespBody<>();
        int begin = dto.getPage() - 1;
        Pageable page = PageRequest.of(begin, dto.getSize());
        PageVo<SuggestionVo> vo = new PageVo<>(dto);
        Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            predicates.add(cb.equal(root.get("level").as(Byte.class), dto.getLevel()));
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            //?????????????????????????????????
            predicates.add(cb.notEqual(root.get("status").as(Boolean.class), SuggestionStatusEnum.HAS_BEEN_REVOKE.getValue()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (dto.getLevel().equals(LevelEnum.TOWN.getValue())){  //?????????????????????
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            if (dto.getStatus() != null){
                if (dto.getStatus().equals(MobileSugStatusEnum.All.getValue())){  //?????????
                    predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_AUDIT.getValue()));
                }else if (dto.getStatus().equals(MobileSugStatusEnum.TO_BE_AUDITED.getValue())){  //?????????
                    Predicate or = cb.or(cb.equal(root.get("status"), SuggestionStatusEnum.SELF_HANDLE.getValue()),cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.AUDIT_FAILURE.getValue()));
                    predicates.add(or);
                }
            }
            Predicate[] p = new Predicate[predicates.size()];
            query.where(cb.and(predicates.toArray(p)));
            query.orderBy(cb.asc(root.get("view")),cb.asc(root.get("status")),cb.desc(root.get("createTime")));
            return query.getRestriction();
        }, page);
        vo.setContent(pageRes.stream().map(SuggestionVo::convert).collect(Collectors.toList()));
        vo.copy(pageRes);
        body.setData(vo);
        return body;
    }

    /**
     * ????????????????????????????????????????????????
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

    //??????????????????????????????????????????????????????
    private void scanSuggestion(NpcMember member) {
        //??????????????????????????????
        List<Suggestion> suggestionList = suggestionRepository.findByRaiserUid(member.getUid());
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.MINUTE, -5);// 5?????????????????????
        Date beforeDate = beforeTime.getTime();
        for (Suggestion suggestion : suggestionList) {
            //???????????????????????????5??????????????????
            if (!suggestion.getRaiseTime().after(beforeDate) || suggestion.getView()){
                suggestion.setCanOperate(false);
            }
        }
        suggestionRepository.saveAll(suggestionList);
    }
}
