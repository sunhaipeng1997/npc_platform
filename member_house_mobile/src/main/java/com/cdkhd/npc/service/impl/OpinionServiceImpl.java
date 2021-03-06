package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.OpinionListVo;
import com.cdkhd.npc.entity.vo.OpinionVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.MsgTypeEnum;
import com.cdkhd.npc.enums.ReplayStatusEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.member_house.OpinionImageRepository;
import com.cdkhd.npc.repository.member_house.OpinionReplayRepository;
import com.cdkhd.npc.repository.member_house.OpinionRepository;
import com.cdkhd.npc.service.OpinionService;
import com.cdkhd.npc.service.PushMessageService;
import com.cdkhd.npc.util.ImageUploadUtil;
import com.cdkhd.npc.utils.NpcMemberUtil;
import com.cdkhd.npc.vo.PageVo;
import com.cdkhd.npc.vo.RespBody;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OpinionServiceImpl implements OpinionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpinionServiceImpl.class);

    private AccountRepository accountRepository;

    private NpcMemberRepository npcMemberRepository;

    private OpinionRepository opinionRepository;

    private OpinionImageRepository opinionImageRepository;

    private OpinionReplayRepository opinionReplayRepository;

    private PushMessageService pushMessageService;

    private SystemSettingRepository systemSettingRepository;

    @Autowired
    public OpinionServiceImpl(AccountRepository accountRepository, NpcMemberRepository npcMemberRepository, OpinionRepository opinionRepository, OpinionImageRepository opinionImageRepository, OpinionReplayRepository opinionReplayRepository, PushMessageService pushMessageService, SystemSettingRepository systemSettingRepository) {
        this.accountRepository = accountRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.opinionRepository = opinionRepository;
        this.opinionImageRepository = opinionImageRepository;
        this.opinionReplayRepository = opinionReplayRepository;
        this.pushMessageService = pushMessageService;
        this.systemSettingRepository = systemSettingRepository;
    }

    @Override
    public RespBody addOpinion(MobileUserDetailsImpl userDetails, AddOpinionDto addOpinionDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(addOpinionDto.getContent())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("?????????????????????????????????");
            LOGGER.error("????????????????????????");
            return body;
        }
        if (StringUtils.isEmpty(addOpinionDto.getReceiver())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????????????????");
            LOGGER.error("?????????????????????????????????");
            return body;
        }
        NpcMember npcMember = npcMemberRepository.findByUid(addOpinionDto.getReceiver());
        if (npcMember.getCanOpinion().equals(StatusEnum.DISABLED.getValue())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("???????????????????????????????????????????????????");
            return body;
        }
        if (StringUtils.isEmpty(addOpinionDto.getTransUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("??????????????????????????????????????????");
            LOGGER.error("??????????????????uid");
            return body;
        }
        //????????????
        SystemSetting systemSetting = null;
        Account account = accountRepository.findByUid(userDetails.getUid());
        if (npcMember.getLevel().equals(LevelEnum.AREA.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndAreaUid(npcMember.getLevel(), npcMember.getArea().getUid());
            if (systemSetting != null && !systemSetting.getVoterOpinionToAll()){//??????????????????????????????????????????
                if (!account.getVoter().getTown().getUid().equals(npcMember.getTown().getUid()) && npcMember.getLevel().equals(LevelEnum.AREA.getValue())){
                    body.setStatus(HttpStatus.BAD_REQUEST);
                    body.setMessage("????????????????????????????????????");
                    LOGGER.error("?????????????????????????????????");
                    return body;
                }
            }
        }else if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue())){
            systemSetting = systemSettingRepository.findByLevelAndTownUid(npcMember.getLevel(), npcMember.getTown().getUid());
            if (systemSetting != null && !systemSetting.getVoterOpinionToAll()){//??????????????????????????????????????????
                if (npcMember.getLevel().equals(LevelEnum.TOWN.getValue()) && account.getVoter().getVillage().getNpcMemberGroup() != null && !account.getVoter().getVillage().getNpcMemberGroup().getUid().equals(npcMember.getNpcMemberGroup().getUid())){
                    body.setStatus(HttpStatus.BAD_REQUEST);
                    body.setMessage("???????????????????????????????????????");
                    LOGGER.error("????????????????????????????????????");
                    return body;
                }
            }
        }

        if (systemSetting != null && !systemSetting.getMemberOpinionToMember()){//????????????????????????
            NpcMember accountIdentity = NpcMemberUtil.getCurrentIden(npcMember.getLevel(), account.getNpcMembers());
            if (accountIdentity != null){
                body.setStatus(HttpStatus.BAD_REQUEST);
                body.setMessage("????????????????????????????????????");
                LOGGER.error("?????????????????????????????????");
                return body;
            }
        }

        Opinion opinion = opinionRepository.findByTransUid(addOpinionDto.getTransUid());
        if (opinion == null) {
            opinion = new Opinion();
            opinion.setArea(userDetails.getArea());
            opinion.setTown(userDetails.getTown());
            opinion.setLevel(addOpinionDto.getLevel());
            opinion.setView(false);//??????????????????????????????????????????????????????
            opinion.setContent(addOpinionDto.getContent());//????????????
            opinion.setReceiver(npcMember);//????????????
            opinion.setStatus(ReplayStatusEnum.UNANSWERED.getValue());//????????????
            opinion.setTransUid(addOpinionDto.getTransUid());
            opinion.setSender(account);
        }
        opinionRepository.saveAndFlush(opinion);
        if (addOpinionDto.getImage() != null) {
            this.saveImg(addOpinionDto.getImage(),opinion);
        }
        Account receiver = npcMember.getAccount();
        //?????????????????????????????????????????????
        if (receiver != null) {
            JSONObject opinionMsg = new JSONObject();
            opinionMsg.put("subtitle", "?????????????????????????????????????????????????????????");
            opinionMsg.put("accountName", opinion.getSender().getVoter().getRealname());
            opinionMsg.put("mobile", opinion.getSender().getMobile());
            opinionMsg.put("content", opinion.getContent());
            opinionMsg.put("remarkInfo", "?????????????????????????????????");
            pushMessageService.pushMsg(receiver, MsgTypeEnum.NEW_OPINION_OR_SUGGESTION.ordinal(), opinionMsg);
        }
        return body;
    }


    /**
     * ????????????
     * @param image
     * @param opinion
     */
    public void saveImg(MultipartFile image, Opinion opinion){
        //???????????????????????????
        String url = ImageUploadUtil.saveImage("opinionImage", image,500,500);
        if (url.equals("error")) {
            LOGGER.error("?????????????????????????????????");
        }
        //????????????????????????
        OpinionImage opinionImage = new OpinionImage();
        opinionImage.setOpinion(opinion);
        opinionImage.setPicture(url);
        opinionImageRepository.saveAndFlush(opinionImage);
    }

    /**
     * ?????????????????????
     * @param userDetails
     * @param opinionDto
     * @return
     */
    @Override
    public RespBody myOpinions(MobileUserDetailsImpl userDetails, OpinionDto opinionDto) {
        RespBody body = new RespBody();
        int begin = opinionDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, opinionDto.getSize());
        Page<Opinion> opinions = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("sender").get("uid").as(String.class), userDetails.getUid()));
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (opinionDto.getLevel().equals(LevelEnum.TOWN.getValue())){
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            //?????? ?????????  ?????????
            if (opinionDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("status").as(Byte.class), opinionDto.getStatus()));
            }
            Predicate[] p = new Predicate[predicates.size()];
            query.where(cb.and(predicates.toArray(p)));
            query.orderBy(cb.desc(root.get("view")),cb.desc(root.get("status")),cb.desc(root.get("createTime")));
            return query.getRestriction();
        }, page);
        List<OpinionListVo> opinionVos = opinions.getContent().stream().map(OpinionListVo::convert).sorted(Comparator.comparing(OpinionListVo::getMyView)).collect(Collectors.toList());
        PageVo<OpinionListVo> vo = new PageVo<>(opinions, opinionDto);
        vo.setContent(opinionVos);
        body.setData(vo);
        return body;
    }

    /**
     * ????????????
     * @param opinionDetailDto
     * @return
     */
    @Override
    public RespBody detailOpinion(OpinionDetailDto opinionDetailDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(opinionDetailDto.getUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("?????????????????????");
            LOGGER.error("??????uid??????");
            return body;
        }
        OpinionVo opinionVo = this.opinionInfo(opinionDetailDto.getUid(),body,opinionDetailDto.getMember());
        body.setData(opinionVo);
        return body;
    }

    public OpinionVo opinionInfo(String uid, RespBody body, Boolean member) {
        Opinion opinion = opinionRepository.findByUid(uid);
        OpinionVo opinionVo = new OpinionVo();
        if (opinion == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("?????????????????????");
            return opinionVo;
        }
        if (member){
            opinion.setView(true);
        }else{
            for (OpinionReply reply : opinion.getReplies()) {
                reply.setView(true);
            }
        }
        opinionRepository.saveAndFlush(opinion);
        opinionVo = OpinionVo.convert(opinion);
        return opinionVo;
    }

    //??????????????????

    /**
     * ??????????????????
     * @param userDetails
     * @param opinionDto
     * @return
     */
    @Override
    public RespBody receiveOpinions(MobileUserDetailsImpl userDetails, OpinionDto opinionDto) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(opinionDto.getLevel(), account.getNpcMembers());
        int begin = opinionDto.getPage() - 1;

        Sort.Order viewSort = new Sort.Order(Sort.Direction.ASC, "view");//????????????????????????
        Sort.Order statusSort = new Sort.Order(Sort.Direction.ASC, "status");//??????????????????
        Sort.Order createAt = new Sort.Order(Sort.Direction.DESC, "createTime");//????????????????????????
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(viewSort);
        orders.add(statusSort);
        orders.add(createAt);
        Sort sort = Sort.by(orders);

        Pageable page = PageRequest.of(begin, opinionDto.getSize(),sort);
        Page<Opinion> opinions = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("receiver").get("uid").as(String.class), npcMember.getUid()));
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), npcMember.getArea().getUid()));
            if (opinionDto.getLevel().equals(LevelEnum.TOWN.getValue())){
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), npcMember.getTown().getUid()));
            }
            //?????? ?????????  ?????????
            if (opinionDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("status").as(Byte.class), opinionDto.getStatus()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        List<OpinionListVo> opinionVos = opinions.getContent().stream().map(OpinionListVo::convert).collect(Collectors.toList());
        PageVo<OpinionListVo> vo = new PageVo<>(opinions, opinionDto);
        vo.setContent(opinionVos);
        body.setData(vo);
        return body;
    }

    /**
     * ????????????
     * @param opinionReplyDto
     * @return
     */
    @Override
    public RespBody replyOpinion(OpinionReplyDto opinionReplyDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(opinionReplyDto.getUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("?????????????????????");
        }
        Opinion opinion = opinionRepository.findByUid(opinionReplyDto.getUid());
        if (opinion == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("?????????????????????");
        }
        opinion.setStatus(ReplayStatusEnum.ANSWERED.getValue());
        opinionRepository.saveAndFlush(opinion);
        OpinionReply opinionReply = new OpinionReply();
        opinionReply.setOpinion(opinion);
        opinionReply.setReply(opinionReplyDto.getContent());
        opinionReplayRepository.saveAndFlush(opinionReply);

        Account sender = opinion.getSender();//????????????????????????????????????????????????
        if (sender != null){
            JSONObject suggestionMsg = new JSONObject();
            suggestionMsg.put("subtitle","????????????????????????????????????????????????????????????");
            suggestionMsg.put("title",opinion.getContent());
            suggestionMsg.put("content",opinionReply.getReply());
            suggestionMsg.put("remarkInfo","????????????"+ opinion.getReceiver().getName()+" <??????????????????>");
            pushMessageService.pushMsg(sender, MsgTypeEnum.FEEDBACK.ordinal(),suggestionMsg);
        }
        return body;
    }

    @Override
    public RespBody memberRecOpins(UidDto uidDto) {
        RespBody body = new RespBody();
        int begin = uidDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, uidDto.getSize(), Sort.Direction.fromString(uidDto.getDirection()), uidDto.getProperty());
        Page<Opinion> opinions = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("receiver").get("uid").as(String.class), uidDto.getUid()));
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        List<OpinionListVo> opinionVos = opinions.getContent().stream().map(OpinionListVo::convert).collect(Collectors.toList());
        body.setData(opinionVos);
        return body;
    }
}
