package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.*;
import com.cdkhd.npc.entity.vo.OpinionListVo;
import com.cdkhd.npc.entity.vo.OpinionVo;
import com.cdkhd.npc.entity.vo.PerformanceListVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.ReplayStatusEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.member_house.OpinionImageRepository;
import com.cdkhd.npc.repository.member_house.OpinionReplayRepository;
import com.cdkhd.npc.repository.member_house.OpinionRepository;
import com.cdkhd.npc.service.OpinionService;
import com.cdkhd.npc.service.PushService;
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

    private PushService pushService;

    @Autowired
    public OpinionServiceImpl(AccountRepository accountRepository, NpcMemberRepository npcMemberRepository, OpinionRepository opinionRepository, OpinionImageRepository opinionImageRepository, OpinionReplayRepository opinionReplayRepository, PushService pushService) {
        this.accountRepository = accountRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.opinionRepository = opinionRepository;
        this.opinionImageRepository = opinionImageRepository;
        this.opinionReplayRepository = opinionReplayRepository;
        this.pushService = pushService;
    }

    @Override
    public RespBody addOpinion(UserDetailsImpl userDetails, AddOpinionDto addOpinionDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(addOpinionDto.getContent())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("请输入您要提交的意见！");
            LOGGER.error("意见内容不能为空");
            return body;
        }
        if (StringUtils.isEmpty(addOpinionDto.getReceiver())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("请选择您要提交意见的代表！");
            LOGGER.error("没有选择接收意见的代表");
            return body;
        }
        NpcMember npcMember = npcMemberRepository.findByUid(addOpinionDto.getReceiver());
        if (npcMember.getCanOpinion().equals(StatusEnum.DISABLED.getValue())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("该代表意见箱已满，请另选一位代表！");
            return body;
        }
        if (StringUtils.isEmpty(addOpinionDto.getTransUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("系统参数错误，请联系管理员！");
            LOGGER.error("没有传入意见uid");
            return body;
        }
        Account account = accountRepository.findByUid(userDetails.getUid());
        Opinion opinion = opinionRepository.findByTransUid(addOpinionDto.getTransUid());
        if (opinion == null) {
            opinion = new Opinion();
            opinion.setArea(userDetails.getArea());
            opinion.setTown(userDetails.getTown());
            opinion.setLevel(userDetails.getLevel());
            opinion.setView(false);//接受代表是否查阅，默认新提交的未查阅
            opinion.setContent(addOpinionDto.getContent());//意见内容
            opinion.setReceiver(npcMember);//接受代表
            opinion.setStatus(ReplayStatusEnum.UNANSWERED.getValue());//是否回复
            opinion.setTransUid(addOpinionDto.getTransUid());
            opinion.setSender(account);
        }
        opinionRepository.saveAndFlush(opinion);
        if (addOpinionDto.getImage() != null) {
            this.saveImg(addOpinionDto.getImage(),opinion);
        }
        //给对应的接受代表推送服务号信息
        String msg = "您有一条新的消息，请前往小程序查看。";
        pushService.pushMsg(account, msg, 3, "选民意见");
        return body;
    }


    /**
     * 保存图片
     * @param image
     * @param opinion
     */
    public void saveImg(MultipartFile image, Opinion opinion){
        //保存图片到文件系统
        String url = ImageUploadUtil.saveImage("opinionImage", image,500,500);
        if (url.equals("error")) {
            LOGGER.error("保存图片到文件系统失败");
        }
        //保存图片到数据库
        OpinionImage opinionImage = new OpinionImage();
        opinionImage.setOpinion(opinion);
        opinionImage.setPicture(url);
        opinionImageRepository.saveAndFlush(opinionImage);
    }

    /**
     * 我提出过的意见
     * @param userDetails
     * @param opinionDto
     * @return
     */
    @Override
    public RespBody myOpinions(UserDetailsImpl userDetails, OpinionDto opinionDto) {
        RespBody body = new RespBody();
        int begin = opinionDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, opinionDto.getSize(), Sort.Direction.fromString(opinionDto.getDirection()), opinionDto.getProperty());
        Page<Opinion> opinions = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("sender").get("uid").as(String.class), "751806ea2d4211ea8f3f0242ac170005"));
            if (opinionDto.getLevel().equals(LevelEnum.TOWN.getValue())){
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }else if (opinionDto.getLevel().equals(LevelEnum.AREA.getValue())){
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            }
            //状态 已回复  未回复
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
     * 意见详情
     * @param opinionDetailDto
     * @return
     */
    @Override
    public RespBody detailOpinion(OpinionDetailDto opinionDetailDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(opinionDetailDto.getUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该意见！");
            LOGGER.error("意见uid为空");
            return body;
        }
        OpinionVo opinionVo = this.opinionInfo(opinionDetailDto.getUid(),body,opinionDetailDto.getMember());
        body.setData(opinionVo);
        return body;
    }

    public OpinionVo opinionInfo(String uid,RespBody body,Boolean member) {
        Opinion opinion = opinionRepository.findByUid(uid);
        OpinionVo opinionVo = new OpinionVo();
        if (opinion == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该意见！");
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

    //代表意见部分

    /**
     * 我收到的意见
     * @param userDetails
     * @param opinionDto
     * @return
     */
    @Override
    public RespBody receiveOpinions(UserDetailsImpl userDetails, OpinionDto opinionDto) {
        RespBody body = new RespBody();
        Account account = accountRepository.findByUid(userDetails.getUid());
        NpcMember npcMember = NpcMemberUtil.getCurrentIden(opinionDto.getLevel(), account.getNpcMembers());
        int begin = opinionDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, opinionDto.getSize(), Sort.Direction.fromString(opinionDto.getDirection()), opinionDto.getProperty());
        Page<Opinion> opinions = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("receiver").get("uid").as(String.class), npcMember.getUid()));
            if (opinionDto.getLevel().equals(LevelEnum.TOWN.getValue())){
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }else if (opinionDto.getLevel().equals(LevelEnum.AREA.getValue())){
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            }
            //状态 已回复  未回复
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
     * 回复意见
     * @param opinionReplyDto
     * @return
     */
    @Override
    public RespBody replyOpinion(OpinionReplyDto opinionReplyDto) {
        RespBody body = new RespBody();
        if (StringUtils.isEmpty(opinionReplyDto.getUid())){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该意见！");
        }
        Opinion opinion = opinionRepository.findByUid(opinionReplyDto.getUid());
        if (opinion == null){
            body.setStatus(HttpStatus.BAD_REQUEST);
            body.setMessage("找不到该意见！");
        }
        opinion.setStatus(ReplayStatusEnum.ANSWERED.getValue());
        opinionRepository.saveAndFlush(opinion);
        OpinionReply opinionReply = new OpinionReply();
        opinionReply.setOpinion(opinion);
        opinionReply.setReply(opinionReplyDto.getContent());
        opinionReplayRepository.saveAndFlush(opinionReply);
        return body;
    }

    @Override
    public RespBody memberRecOpins(UidDto uidDto) {
        RespBody body = new RespBody();
        int begin = uidDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, uidDto.getSize(), Sort.Direction.fromString(uidDto.getDirection()), uidDto.getProperty());
        Page<Opinion> opinions = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("receiver").get("uid").as(String.class), uidDto.getUid()));
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, page);
        List<OpinionListVo> opinionVos = opinions.getContent().stream().map(OpinionListVo::convert).collect(Collectors.toList());
        body.setData(opinionVos);
        return body;
    }
}
