package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.AccountRepository;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.NpcMemberRoleRepository;
import com.cdkhd.npc.repository.base.SystemSettingRepository;
import com.cdkhd.npc.repository.member_house.*;
import com.cdkhd.npc.service.*;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RankServiceImpl implements RankService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RankServiceImpl.class);

    private PerformanceRepository performanceRepository;

    private PerformanceTypeRepository performanceTypeRepository;

    private SystemSettingRepository systemSettingRepository;

    private NpcMemberRepository npcMemberRepository;

    private NpcMemberRoleRepository npcMemberRoleRepository;

    private AccountRepository accountRepository;

    private PerformanceImageRepository performanceImageRepository;

    private NpcMemberRoleService npcMemberRoleService;

    private SystemSettingService systemSettingService;

    private SuggestionRepository suggestionRepository;

    private OpinionRepository opinionRepository;

    @Autowired
    public RankServiceImpl(PerformanceRepository performanceRepository, PerformanceTypeRepository performanceTypeRepository, SystemSettingRepository systemSettingRepository, NpcMemberRepository npcMemberRepository, NpcMemberRoleRepository npcMemberRoleRepository, AccountRepository accountRepository, PerformanceImageRepository performanceImageRepository, NpcMemberRoleService npcMemberRoleService, SystemSettingService systemSettingService, SuggestionRepository suggestionRepository, OpinionRepository opinionRepository) {
        this.performanceRepository = performanceRepository;
        this.performanceTypeRepository = performanceTypeRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.npcMemberRoleRepository = npcMemberRoleRepository;
        this.accountRepository = accountRepository;
        this.performanceImageRepository = performanceImageRepository;
        this.npcMemberRoleService = npcMemberRoleService;
        this.systemSettingService = systemSettingService;
        this.suggestionRepository = suggestionRepository;
        this.opinionRepository = opinionRepository;
    }

    /**
     * 代表建议排名
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody memberSuggestionRank(UserDetailsImpl userDetails, Byte level) {
        RespBody body = new RespBody();
        List<NpcMember> npcMembers = this.getMembers(userDetails,level);
        List<Suggestion> suggestions = this.getSuggestions(userDetails,level);
        Map<String,String> memberMap = this.dealNpcMember(npcMembers);
        Map<String,Integer> suggestionMap = this.dealSuggestions(suggestions,true);
        Map<String, Integer> nameMap = Maps.newHashMap();
        for (String key : memberMap.keySet()) {
            nameMap.put(memberMap.get(key),suggestionMap.getOrDefault(key,0));
        }
        body.setData(nameMap);
        return body;
    }

    /**
     * 各镇建议排名
     * @param userDetails
     * @param level 当前所选等级
     * @return
     */
    @Override
    public RespBody townSuggestionRank(UserDetailsImpl userDetails, Byte level) {
        RespBody body = new RespBody();
        List<Town> towns = Lists.newArrayList(userDetails.getArea().getTowns());
        List<Suggestion> suggestions = this.getSuggestions(userDetails,level);
        Map<String,String> townMap = this.dealTowns(towns);
        Map<String,Integer> suggestionMap = this.dealSuggestions(suggestions,false);
        Map<String, Integer> nameMap = Maps.newHashMap();
        for (String key : townMap.keySet()) {
            nameMap.put(townMap.get(key),suggestionMap.getOrDefault(key,0));
        }
        body.setData(nameMap);
        return body;
    }

    /**
     * 代表收到的意见排名
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody memberOpinionRank(UserDetailsImpl userDetails, Byte level) {
        RespBody body = new RespBody();
        List<NpcMember> npcMembers = this.getMembers(userDetails,level);
        List<Opinion> opinions = this.getOpinions(userDetails, level);
        Map<String,String> memberMap = this.dealNpcMember(npcMembers);
        Map<String,Integer> opinionsMap = this.dealOpinions(opinions,true);
        Map<String, Integer> nameMap = Maps.newHashMap();
        for (String key : memberMap.keySet()) {
            nameMap.put(memberMap.get(key),opinionsMap.getOrDefault(key,0));
        }
        body.setData(nameMap);
        return body;
    }

    /**
     * 各镇收到的意见排名
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody townOpinionRank(UserDetailsImpl userDetails, Byte level) {
        RespBody body = new RespBody();
        List<Town> towns = Lists.newArrayList(userDetails.getArea().getTowns());
        List<Opinion> opinions = this.getOpinions(userDetails, level);
        Map<String,String> townMap = this.dealTowns(towns);
        Map<String,Integer> opinionsMap = this.dealOpinions(opinions,false);
        Map<String, Integer> nameMap = Maps.newHashMap();
        for (String key :townMap.keySet()) {
            nameMap.put(townMap.get(key),opinionsMap.getOrDefault(key,0));
        }
        body.setData(nameMap);
        return body;
    }

    /**
     * 代表履职排名
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody memberPerformanceRank(UserDetailsImpl userDetails, Byte level) {
        RespBody body = new RespBody();
        List<NpcMember> npcMembers = this.getMembers(userDetails,level);
        List<Performance> performances = this.getPerformance(userDetails, level);
        Map<String,String> npcMemberMap = this.dealNpcMember(npcMembers);
        Map<String,Integer> performanceMap = this.dealPerformance(performances,true);
        Map<String, Integer> nameMap = Maps.newHashMap();
        for (String key :npcMemberMap.keySet()) {
            nameMap.put(npcMemberMap.get(key), performanceMap.getOrDefault(key,0));
        }
        body.setData(nameMap);
        return body;
    }

    private Map<String, Integer> dealPerformance(List<Performance> performances, boolean isPerson) {
        Map<String,Integer> performanceMap = Maps.newHashMap();
        for (Performance performance : performances) {
            String uid;
            if (isPerson){
                uid = performance.getNpcMember().getUid();
            }else{
                uid = performance.getTown().getUid();
            }
            Integer number = performanceMap.getOrDefault(uid,0);
            performanceMap.put(uid,number+1);
        }
        return performanceMap;
    }

    /**
     * 各镇履职情况排名
     *
     * @param userDetails
     * @return
     */
    @Override
    public RespBody townPerformanceRank(UserDetailsImpl userDetails, Byte level) {
        RespBody body = new RespBody();
        List<Town> towns = Lists.newArrayList(userDetails.getArea().getTowns());
        List<Performance> performances = this.getPerformance(userDetails, level);
        Map<String,String> townMap = this.dealTowns(towns);
        Map<String,Integer> performanceMap = this.dealPerformance(performances,false);
        Map<String, Integer> nameMap = Maps.newHashMap();
        for (String key :townMap.keySet()) {
            nameMap.put(townMap.get(key),performanceMap.getOrDefault(key,0));
        }
        body.setData(nameMap);
        return body;
    }

    /**
     * 获取代表列表
     *
     * @param userDetails
     * @return
     */
    private List<NpcMember> getMembers(UserDetailsImpl userDetails, Byte level) {
        List<NpcMember> npcMemberList = Lists.newArrayList();
        if (level.equals(LevelEnum.TOWN.getValue())) {//等级为镇上，获取所有镇代表
            npcMemberList = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(userDetails.getTown().getUid(), level);
        }
        if (level.equals(LevelEnum.AREA.getValue())) {
            npcMemberList = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(), level);
        }
        return npcMemberList;
    }

    private List<Suggestion> getSuggestions(UserDetailsImpl userDetails,Byte level) {
        List<Suggestion> suggestionList = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), level));
            if (level.equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            }
            predicates.add(cb.greaterThanOrEqualTo(root.get("status").as(Byte.class),(byte)3));//todo 建议状态在提交到政府以后
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return suggestionList;
    }

    private List<Opinion> getOpinions(UserDetailsImpl userDetails, Byte level){
        List<Opinion> opinionList = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), level));
            if (level.equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return opinionList;
    }

    /**
     * 返回代表的uid和name
     * @param npcMembers
     * @return
     */
    private Map<String, String> dealNpcMember(List<NpcMember> npcMembers){
        Map<String,String> menberMap = Maps.newHashMap();
        for (NpcMember npcMember : npcMembers) {
            menberMap.put(npcMember.getUid(),npcMember.getName());
        }
        return menberMap;
    }
    /**
     * 返回代表的uid和name
     * @param towns
     * @return
     */
    private Map<String, String> dealTowns(List<Town> towns){
        Map<String,String> townMap = Maps.newHashMap();
        for (Town town: towns) {
            townMap.put(town.getUid(), town.getName());
        }
        return townMap;
    }

    /**
     * 获取代表uid和建议数量的map
     * @param suggestions
     * @param isPerson 是否按人统计 true key：memberUid false key townUid
     * @return
     */
    private Map<String,Integer> dealSuggestions(List<Suggestion> suggestions,Boolean isPerson){
        Map<String,Integer> suggestionMaps = Maps.newHashMap();
        for (Suggestion suggestion : suggestions) {
            String uid;
            if (isPerson) {
                uid = suggestion.getRaiser().getUid();
            }else{
                uid = suggestion.getTown().getUid();
            }
            Integer number = suggestionMaps.getOrDefault(uid,0);
            suggestionMaps.put(uid, number+1);
        }
        return suggestionMaps;
    }

    private Map<String,Integer> dealOpinions(List<Opinion> opinions, Boolean isPerson){
        Map<String, Integer> opinionMaps = Maps.newHashMap();
        for (Opinion opinion : opinions) {
            String uid;
            if (isPerson) {
                uid = opinion.getReceiver().getUid();
            }else{
                uid = opinion.getTown().getUid();
            }
            Integer number = opinionMaps.getOrDefault(uid,0);
            opinionMaps.put(uid,number+1);
        }
        return opinionMaps;
    }

    private List<Performance> getPerformance(UserDetailsImpl userDetails, Byte level){
        List<Performance> performances = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), level));
            predicates.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));
            if (level.equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return performances;
    }
}