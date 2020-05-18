package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.SugAddDto;
import com.cdkhd.npc.entity.dto.SugAuditDto;
import com.cdkhd.npc.entity.dto.SugPageDto;
import com.cdkhd.npc.entity.vo.SugDetailVo;
import com.cdkhd.npc.enums.MobileSugStatusEnum;
import com.cdkhd.npc.enums.NpcSugStatusEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionImageRepository;
import com.cdkhd.npc.repository.member_house.SuggestionReplyRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.NpcSuggestionService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
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

    @Autowired
    public NpcSuggestionServiceImpl(AccountRepository accountRepository,
                                    SuggestionRepository suggestionRepository,
                                    SuggestionBusinessRepository suggestionBusinessRepository,
                                    SuggestionImageRepository suggestionImageRepository,
                                    SuggestionReplyRepository suggestionReplyRepository) {
        this.accountRepository = accountRepository;
        this.suggestionRepository = suggestionRepository;
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.suggestionImageRepository = suggestionImageRepository;
        this.suggestionReplyRepository = suggestionReplyRepository;
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
    public RespBody updateSuggestion(MobileUserDetailsImpl userDetails, SugAddDto sugAddDto) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugAddDto.getUid());
        if (suggestion == null){
            body.setMessage("此建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        suggestion = suggestionRepository.findByUidAndTransUid(sugAddDto.getUid(),sugAddDto.getTransUid());
        if (null == suggestion){  //第一次提交且有附件就删除之前的图片
            suggestion = suggestionRepository.findByUid(sugAddDto.getUid());
            Set<SuggestionImage> images = suggestion.getSuggestionImages();
            suggestionImageRepository.deleteAll(images);
            suggestion.setTransUid(sugAddDto.getTransUid());
            suggestion.setCanOperate(true);
            suggestion.setStatus(SuggestionStatusEnum.SUBMITTED_AUDIT.getValue());
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
        }
        return body;
    }

    @Override
    public RespBody submitSuggestion(String sugUid) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugUid);
        if (suggestion == null){
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
        if (suggestion == null){
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Date expireAt = DateUtils.addMinutes(suggestion.getRaiseTime(), 5);
        if (expireAt.before(new Date())){
            body.setMessage("该建议已经提交超过5分钟，无法撤回");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (suggestion.getView()){
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
    public RespBody suggestionDetail(String sugUid) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugUid);
        if (suggestion == null){
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        SugDetailVo sugDetailVo = SugDetailVo.convert(suggestion);
        body.setData(sugDetailVo);
        return body;
    }

    @Override
    public RespBody deleteSuggestion(String sugUid) {
        RespBody body = new RespBody();
        Suggestion suggestion = suggestionRepository.findByUid(sugUid);
        if (suggestion == null){
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        if (!suggestion.getStatus().equals(SuggestionStatusEnum.HAS_BEEN_REVOKE.getValue()) &&
                !suggestion.getStatus().equals(SuggestionStatusEnum.NOT_SUBMITTED.getValue()) &&
                !suggestion.getStatus().equals(SuggestionStatusEnum.AUDIT_FAILURE.getValue())){
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
        if (suggestion == null){
            body.setMessage("该建议不存在");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(sugAuditDto.getLevel(), account.getNpcMembers());
        if (sugAuditDto.getStatus().equals(StatusEnum.ENABLED.getValue())){  //接受
            suggestion.setStatus(SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue());  //将建议状态设置成“已提交政府”
        }else {
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

    @Override
    public RespBody npcMemberSug(MobileUserDetailsImpl userDetails, SugPageDto sugPageDto) {
        RespBody body = new RespBody<>();
        int begin = sugPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, sugPageDto.getSize());
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(sugPageDto.getLevel(), account.getNpcMembers());
//        scanSuggestion(npcMember);  //扫描建议
        PageVo<SugDetailVo> vo = new PageVo<>(sugPageDto);
        if (npcMember != null) {
            Page<Suggestion> pageRes = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
                predicates.add(cb.equal(root.get("raiser").get("uid").as(String.class), npcMember.getUid()));
                if (sugPageDto.getStatus() != NpcSugStatusEnum.All.getValue()){  //分类
                    if (sugPageDto.getStatus().equals(NpcSugStatusEnum.TO_BE_AUDITED.getValue())){  //草稿
                        predicates.add(cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.NOT_SUBMITTED.getValue()));
                    }else if (sugPageDto.getStatus().equals(NpcSugStatusEnum.TO_BE_AUDITED.getValue())){  //已审核
                        predicates.add(cb.notEqual(root.get("status").as(Byte.class), MobileSugStatusEnum.TO_BE_AUDITED.getValue()));
                        predicates.add(cb.notEqual(root.get("status").as(Byte.class), SuggestionStatusEnum.HAS_BEEN_REVOKE.getValue()));
                    }
                }
                Predicate[] p = new Predicate[predicates.size()];
                query.where(cb.and(predicates.toArray(p)));
                query.orderBy(cb.desc(root.get("status")),cb.desc(root.get("raiseTime")));
                return query.getRestriction();
            }, page);
            vo.setContent(pageRes.stream().map(SugDetailVo::convert).sorted(Comparator.comparing(SugDetailVo::getMyView)).collect(Collectors.toList()));
            vo.copy(pageRes);
        }
        body.setData(vo);
        return body;
    }
}
