package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.AddOpinionDto;
import com.cdkhd.npc.entity.dto.AddPerformanceDto;
import com.cdkhd.npc.entity.dto.AddSuggestionDto;
import com.cdkhd.npc.entity.vo.AreaVo;
import com.cdkhd.npc.entity.vo.EdgeVo;
import com.cdkhd.npc.entity.vo.NodeVo;
import com.cdkhd.npc.enums.*;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.AreaRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.repository.member_house.*;
import com.cdkhd.npc.service.ThirdService;
import com.cdkhd.npc.vo.CountVo;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ThirdServiceImpl implements ThirdService {

    private AreaRepository areaRepository;
    private TownRepository townRepository;
    private SuggestionRepository suggestionRepository;
    private SuggestionImageRepository suggestionImageRepositoryy;
    private OpinionRepository opinionRepository;
    private OpinionImageRepository opinionImageRepository;
    private PerformanceRepository performanceRepository;
    private PerformanceImageRepository performanceImageRepository;
    private PerformanceTypeRepository performanceTypeRepository;
    private NpcMemberRepository npcMemberRepository;
    private AccountRepository accountRepository;
    private SuggestionBusinessRepository suggestionBusinessRepository;

    @Autowired
    public ThirdServiceImpl(AreaRepository areaRepository, TownRepository townRepository, SuggestionRepository suggestionRepository, SuggestionImageRepository suggestionImageRepositoryy, OpinionRepository opinionRepository, OpinionImageRepository opinionImageRepository, PerformanceRepository performanceRepository, PerformanceImageRepository performanceImageRepository, PerformanceTypeRepository performanceTypeRepository, NpcMemberRepository npcMemberRepository, AccountRepository accountRepository, SuggestionBusinessRepository suggestionBusinessRepository) {
        this.areaRepository = areaRepository;
        this.townRepository = townRepository;
        this.suggestionRepository = suggestionRepository;
        this.suggestionImageRepositoryy = suggestionImageRepositoryy;
        this.opinionRepository = opinionRepository;
        this.opinionImageRepository = opinionImageRepository;
        this.performanceRepository = performanceRepository;
        this.performanceImageRepository = performanceImageRepository;
        this.performanceTypeRepository = performanceTypeRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.accountRepository = accountRepository;
        this.suggestionBusinessRepository = suggestionBusinessRepository;
    }

    //获取区及其包含的镇
    @Override
    public AreaVo getRelation(UserDetailsImpl userDetails) {
        AreaVo areaVo = new AreaVo();
        Area area = userDetails.getArea();
        List<NodeVo> nodeVos = area.getTowns().stream().filter(town -> town.getStatus().equals(StatusEnum.ENABLED.getValue()) && !town.getIsDel()).map(town -> NodeVo.convert(town.getUid(),town.getName())).collect(Collectors.toList());//将各镇信息加入节点
        nodeVos.add(0,new NodeVo(area.getUid(),area.getName()));//将区信息加入节点
        //设置节点之间的关系
        List<EdgeVo> edgeVos = Lists.newArrayList();
        for (Town town : area.getTowns()) {
            EdgeVo edgeVo = EdgeVo.convert(area.getUid(),town.getUid());
            edgeVos.add(edgeVo);
        }
        areaVo.setNodes(nodeVos);
        areaVo.setEdges(edgeVos);
        return areaVo;
    }

    //统计各镇 / 小组的建议数量
    @Override
    public RespBody countSuggestions4Town(UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody<List<CountVo>> body = new RespBody<>();

        if (level.equals(LevelEnum.AREA.getValue())) {
            body.setData(suggestionRepository.countByArea(userDetails.getArea().getId()));
        } else {
            body.setData(suggestionRepository.countByTown(userDetails.getArea().getId(), uid));
        }

        return body;
    }

    //统计各镇的意见数量
    @Override
    public RespBody countOpinions4Town(UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody<List<CountVo>> body = new RespBody<>();

        if (level.equals(LevelEnum.AREA.getValue())) {
            body.setData(opinionRepository.countByArea(userDetails.getArea().getId()));
        } else {
            body.setData(opinionRepository.countByTown(userDetails.getArea().getId(), uid));
        }

        return body;
    }

    //统计各镇的履职数量
    @Override
    public RespBody countPerformances4Town(UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody<List<CountVo>> body = new RespBody<>();

        if (level.equals(LevelEnum.AREA.getValue())) {
            body.setData(performanceRepository.countByArea(userDetails.getArea().getId()));
        } else {
            body.setData(performanceRepository.countByTown(userDetails.getArea().getId(), uid));
        }

        return body;
    }

    //统计各类型的建议数量
    @Override
    public RespBody countSuggestions4Type(UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody<List<CountVo>> body = new RespBody<>();
        if (level.equals(LevelEnum.AREA.getValue())) {
            body.setData(suggestionRepository.countByAreaType(userDetails.getArea().getId()));
        } else  {
            body.setData(suggestionRepository.countByTownType(userDetails.getArea().getId(), uid));
        }
        return body;
    }

    //统计代表的学历
    @Override
    public RespBody countEducation4NpcMember(UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody<List<CountVo>> body = new RespBody<>();
            if (level.equals(LevelEnum.AREA.getValue())) {
            body.setData(npcMemberRepository.countEducation(userDetails.getArea().getUid(),level));
        } else  {
            body.setData(npcMemberRepository.countEducation(userDetails.getArea().getUid(), userDetails.getTown().getUid(),level));
        }
        return body;
    }



    //统计各镇的建议数量之和，意见数量之和，履职数量之和
    @Override
    public RespBody countAll(UserDetailsImpl userDetails, Byte level, String uid) {
        RespBody<JSONObject> body = new RespBody<>();

        JSONObject object = new JSONObject();

        if (level.equals(LevelEnum.AREA.getValue())) {
            object.put("suggestion", suggestionRepository.countAll4Area(userDetails.getArea().getId()));
            object.put("performance", performanceRepository.countAll4Area(userDetails.getArea().getId()));
            object.put("opinion", opinionRepository.countAll4Area(userDetails.getArea().getId()));
        } else {
            object.put("suggestion", suggestionRepository.countAll4Town(userDetails.getArea().getId(), uid));
            object.put("performance", performanceRepository.countAll4Town(userDetails.getArea().getId(), uid));
            object.put("opinion", opinionRepository.countAll4Town(userDetails.getArea().getId(), uid));
        }

        body.setData(object);
        return body;
    }

    @Override
    public RespBody syncOpinion(List<AddOpinionDto> addOpinionDtos) {
        RespBody body = new RespBody<>();
        for (AddOpinionDto addOpinionDto : addOpinionDtos) {
            Opinion opinion = new Opinion();
            opinion.setArea(areaRepository.findByName(addOpinionDto.getAreaName()));
            opinion.setTown(townRepository.findByName(addOpinionDto.getTownName()));
            opinion.setContent(addOpinionDto.getContent());
            opinion.setLevel(addOpinionDto.getLevel());
            opinion.setView(true);
            opinion.setStatus(ReplayStatusEnum.ANSWERED.getValue());
            opinion.setTransUid(addOpinionDto.getTransUid());
            opinion.setReceiver(npcMemberRepository.findByLevelAndMobileAndIsDelFalse(addOpinionDto.getLevel(), addOpinionDto.getReceiver()));
            opinion.setSender(accountRepository.findByUsername("bigData"));//默认所有的意见提出人都是这个
            opinionRepository.saveAndFlush(opinion);
            if (CollectionUtils.isNotEmpty(addOpinionDto.getImages())) {
                List<OpinionImage> opinionImageList = Lists.newArrayList();
                for (String image : addOpinionDto.getImages()) {
                    OpinionImage opinionImage = new OpinionImage();
                    opinionImage.setOpinion(opinion);
                    opinionImage.setPicture(image);
                    opinionImageList.add(opinionImage);
                }
                opinionImageRepository.saveAll(opinionImageList);
            }
        }
        return body;
    }

    @Override
    public RespBody syncSuggestion(List<AddSuggestionDto> dtos) {
        RespBody body = new RespBody<>();
        for (AddSuggestionDto dto : dtos) {
            Suggestion suggestion = new Suggestion();
            suggestion.setLevel(dto.getLevel());
            suggestion.setArea(areaRepository.findByName(dto.getAreaName()));
            suggestion.setTown(townRepository.findByName(dto.getTownName()));
            NpcMember npcMember = npcMemberRepository.findByLevelAndMobileAndIsDelFalse(dto.getLevel(),dto.getRaiserMobile());
            suggestion.setRaiser(npcMember);
            suggestion.setLeader(npcMember);
            suggestion.setAuditor(npcMemberRepository.findByLevelAndMobileAndIsDelFalse(dto.getLevel(),dto.getAuditorMobile()));
            suggestion.setTransUid(dto.getTransUid());
            suggestion.setTitle(dto.getTitle());
            suggestion.setContent(dto.getContent());
            suggestion.setRaiseTime(dto.getRaiseTime());
            suggestion.setView(true);
            suggestion.setCanOperate(false);
            suggestion.setSuggestionBusiness(suggestionBusinessRepository.findByTownNameAndLevelAndName(dto.getTownName(),dto.getLevel(),dto.getBusiness()));
            suggestion.setStatus(SuggestionStatusEnum.SELF_HANDLE.getValue());  //建议状态改为“自行办理”
            suggestionRepository.saveAndFlush(suggestion);
            if (CollectionUtils.isNotEmpty(dto.getImages())){
                List<SuggestionImage> suggestionImages = Lists.newArrayList();
                for (String image : dto.getImages()) {
                    SuggestionImage suggestionImage = new SuggestionImage();
                    suggestionImage.setTransUid(dto.getTransUid());
                    suggestionImage.setSuggestion(suggestion);
                    suggestionImage.setUrl(image);
                    suggestionImages.add(suggestionImage);
                }
                suggestionImageRepositoryy.saveAll(suggestionImages);
            }
        }
        return body;
    }

    @Override
    public RespBody syncPerformance(List<AddPerformanceDto> dtos) {
        RespBody body = new RespBody<>();
        for (AddPerformanceDto dto : dtos) {
            Performance performance = new Performance();
            performance.setLevel(dto.getLevel());
            performance.setArea(areaRepository.findByName(dto.getAreaName()));
            performance.setTown(townRepository.findByName(dto.getTownName()));
            NpcMember npcMember = npcMemberRepository.findByLevelAndMobileAndIsDelFalse(dto.getLevel(),dto.getRaiserMobile());
            performance.setTransUid(dto.getTransUid());
            performance.setNpcMember(npcMember);
            performance.setStatus(PerformanceStatusEnum.AUDIT_SUCCESS.getValue());//设置为审核通过状态
            performance.setView(true);
            performance.setCanOperate(false);//同步过来的数据不能进行操作
            performance.setPerformanceType(performanceTypeRepository.findByNameAndTownName(dto.getPerformanceType(),dto.getTownName()));
            performance.setTitle(dto.getTitle());
            performance.setWorkAt(dto.getWorkAt());
            performance.setAuditAt(dto.getAuditDate());
            performance.setAuditor(npcMemberRepository.findByLevelAndMobileAndIsDelFalse(dto.getLevel(),dto.getAuditorMobile()));
            performance.setContent(dto.getContent());
            performanceRepository.saveAndFlush(performance);
            if (CollectionUtils.isNotEmpty(dto.getImages())){
                List<PerformanceImage> performanceImages = Lists.newArrayList();
                for (String image : dto.getImages()) {
                    PerformanceImage performanceImage = new PerformanceImage();
                    performanceImage.setPerformance(performance);
                    performanceImage.setTransUid(dto.getTransUid());
                    performanceImage.setUrl(image);
                    performanceImages.add(performanceImage);
                }
                performanceImageRepository.saveAll(performanceImages);
            }
        }
        return body;
    }
}
