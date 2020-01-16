package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.dto.StatisticalPageDto;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.TownRepository;
import com.cdkhd.npc.repository.member_house.PerformanceRepository;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.service.StatisticalService;
import com.cdkhd.npc.service.SystemSettingService;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StatisticalServiceImpl implements StatisticalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticalServiceImpl.class);

    private PerformanceRepository performanceRepository;

    private NpcMemberRepository npcMemberRepository;

    private SystemSettingService systemSettingService;

    private PerformanceTypeRepository performanceTypeRepository;

    private TownRepository townRepository;

    @Autowired
    public StatisticalServiceImpl(PerformanceRepository performanceRepository, NpcMemberRepository npcMemberRepository, SystemSettingService systemSettingService, PerformanceTypeRepository performanceTypeRepository) {
        this.performanceRepository = performanceRepository;
        this.npcMemberRepository = npcMemberRepository;
        this.systemSettingService = systemSettingService;
        this.performanceTypeRepository = performanceTypeRepository;
    }

    /**
     * 代表履职统计
     * @param userDetails
     * @param statisticalPageDto
     * @return
     */
    @Override
    public RespBody memberPerformance(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto) {
        RespBody body = new RespBody();
        //查询代表的履职之前首先查询系统配置
        int begin = statisticalPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, statisticalPageDto.getSize(), Sort.Direction.fromString(statisticalPageDto.getDirection()), statisticalPageDto.getProperty());
        Page<NpcMember> npcMembers = this.getNpcMemberPage(userDetails,statisticalPageDto,page);
        List<Performance> performanceList = this.getPerformanceList(userDetails,statisticalPageDto);
        List<PerformanceType> performanceTypeList = this.getPerformanceTypeList(userDetails);
        Map<String,Map<String, Integer>> memberMap = this.dealPerformanceForMember(npcMembers,performanceList,performanceTypeList);
        body.setData(memberMap);
        return body;
    }



    private Map<String, Map<String, Integer>> dealPerformanceForMember(Page<NpcMember> npcMembers, List<Performance> performanceList, List<PerformanceType> performanceTypeList) {
        Map<String, Map<String, Integer>> memberNameMap = Maps.newHashMap();// key:代表姓名  value:Map<Map:履职类型, Integer:履职条数>
        Map<String, Map<String, Integer>> memberMap = Maps.newHashMap();// key:代表uid  value:Map<Map:履职类型, Integer:履职条数>
        Map<String, String> members = Maps.newHashMap();//// key：代表uid  value：代表姓名    //别问我为啥要存这个，是因为代表可能重名，如依赖就把名字存在上面，那么就不能区分重名的代表了，所以上面先存代表uid，等最后返回前端的时候，再替换成名字
        Map<String, Integer> performanceMap = Maps.newHashMap();
        for (PerformanceType performanceType : performanceTypeList) {//先将所有的履职类型初始化成0个
            performanceMap.put(performanceType.getUid(),0);
        }
        for (NpcMember npcMember : npcMembers) {//所有符合条件的代表信息
            members.put(npcMember.getUid(),npcMember.getName());
            memberMap.put(npcMember.getUid(),performanceMap);
        }
        for (Performance performance : performanceList) {//遍历统计所有查询出来的履职信息
            String npcMemberUid = performance.getNpcMember().getUid();//代表uid
            String performanceUid = performance.getPerformanceType().getUid();//履职类型uid
            Map<String, Integer> performances = memberMap.getOrDefault(npcMemberUid,Maps.newHashMap());//这位代表所有的履职类型和数量
            Integer times = performances.getOrDefault(performanceUid,0)+1;
            performanceMap.put(performanceMap.keySet().toString(),times);
            memberMap.put(npcMemberUid,performances);
        }
        for (String s : memberMap.keySet()) {
            memberNameMap.put(members.get(s),memberMap.get(s));
        }
        return memberMap;
    }

    /**
     * 获取所有可用的履职类型
     * @param userDetails
     * @return
     */
    private List<PerformanceType> getPerformanceTypeList(UserDetailsImpl userDetails) {
        List<PerformanceType> performanceTypeList = Lists.newArrayList();
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
            performanceTypeList = performanceTypeRepository.findByLevelAndTownUidAndStatusAndIsDelFalse(userDetails.getLevel(),userDetails.getTown().getUid(), StatusEnum.ENABLED.getValue());
        }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
            performanceTypeList = performanceTypeRepository.findByLevelAndAreaUidAndStatusAndIsDelFalse(userDetails.getLevel(),userDetails.getArea().getUid(), StatusEnum.ENABLED.getValue());
        }
        return performanceTypeList;
    }

    /**
     * 条件查询代表
     * @param userDetails
     * @param statisticalPageDto
     * @param page
     * @return
     */
    private Page<NpcMember> getNpcMemberPage(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto,Pageable page) {
        Page<NpcMember> npcMemberPage = npcMemberRepository.findAll((Specification<NpcMember>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                //代表小组
                if (StringUtils.isNotEmpty(statisticalPageDto.getAreaUId())) {
                    predicates.add(cb.equal(root.get("npcMemberGroup").get("uid").as(String.class), statisticalPageDto.getAreaUId()));
                }
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                //代表小组
                if (StringUtils.isNotEmpty(statisticalPageDto.getAreaUId())) {
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), statisticalPageDto.getAreaUId()));
                }
            }
            //提出代表
            if (StringUtils.isNotEmpty(statisticalPageDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + statisticalPageDto.getName() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        },page);
        return npcMemberPage;
    }

    /**
     * 条件查询履职
     * @param userDetails
     * @param statisticalPageDto
     * @return
     */
    private List<Performance> getPerformanceList(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto) {
        List<Performance> performanceList = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                SystemSetting systemSetting = systemSettingService.getSystemSetting();
                if (systemSetting.getShowSubPerformance()) {
                    List<NpcMember> members = npcMemberRepository.findByLevel(LevelEnum.TOWN.getValue());
                    List<String> accountUids = Lists.newArrayList();
                    for (NpcMember member : members) {
                        accountUids.add(member.getAccount().getUid());
                    }
                    predicates.add(cb.in(root.get("npcMember").get("account").get("uid")).value(accountUids));
                }
            }
            //提出代表
            if (StringUtils.isNotEmpty(statisticalPageDto.getName())) {
                predicates.add(cb.like(root.get("npcMember").get("name").as(String.class), "%" + statisticalPageDto.getName() + "%"));
            }
            //履职时间 开始
            if (statisticalPageDto.getDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workAt").as(Date.class), statisticalPageDto.getDateStart()));
            }
            if (statisticalPageDto.getDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workAt").as(Date.class), statisticalPageDto.getDateEnd()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return performanceList;
    }


    /**
     * 各镇履职统计
     * @param userDetails
     * @param statisticalPageDto
     * @return
     */
    @Override
    public RespBody townPerformance(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto) {
        RespBody body = new RespBody();
        //查询代表的履职之前首先查询系统配置
        int begin = statisticalPageDto.getPage() - 1;
        Pageable page = PageRequest.of(begin, statisticalPageDto.getSize(), Sort.Direction.fromString(statisticalPageDto.getDirection()), statisticalPageDto.getProperty());
        Page<Town> towns = this.getTownPage(userDetails,statisticalPageDto,page);
        List<Performance> performanceList = this.getPerformanceList(userDetails,statisticalPageDto);
        List<PerformanceType> performanceTypeList = this.getPerformanceTypeList(userDetails);
        Map<String,Map<String, Integer>> townMap = this.dealPerformance(towns,performanceList,performanceTypeList);
        body.setData(townMap);
        return body;
    }

    private Map<String, Map<String, Integer>> dealPerformance(Page<Town> towns, List<Performance> performanceList, List<PerformanceType> performanceTypeList) {
        Map<String, Map<String, Integer>> townNameMap = Maps.newHashMap();// key:镇名称  value:Map<Map:履职类型, Integer:履职条数>
        Map<String, Map<String, Integer>> townMap = Maps.newHashMap();// key:镇uid  value:Map<Map:履职类型, Integer:履职条数>
        Map<String, String> townInfo = Maps.newHashMap();//// key：镇uid  value：镇名称    //别问我为啥要存这个，是因为代表可能重名，如依赖就把名字存在上面，那么就不能区分重名的代表了，所以上面先存代表uid，等最后返回前端的时候，再替换成名字
        Map<String, Integer> performanceMap = Maps.newHashMap();
        for (PerformanceType performanceType : performanceTypeList) {//先将所有的履职类型初始化成0个
            performanceMap.put(performanceType.getUid(),0);
        }
        for (Town town : towns) {//所有符合条件的代表信息
            townInfo.put(town.getUid(),town.getName());
            townMap.put(town.getUid(),performanceMap);
        }
        for (Performance performance : performanceList) {//遍历统计所有查询出来的履职信息
            String townUid = performance.getTown().getUid();//代表uid
            String performanceUid = performance.getPerformanceType().getUid();//履职类型uid
            Map<String, Integer> performances = townMap.getOrDefault(townUid,Maps.newHashMap());//这位代表所有的履职类型和数量
            Integer times = performances.getOrDefault(performanceUid,0)+1;
            performanceMap.put(performanceMap.keySet().toString(),times);
            townMap.put(townUid,performances);
        }
        for (String s : townMap.keySet()) {
            townNameMap.put(townInfo.get(s),townMap.get(s));
        }
        return townMap;
    }


    private Page<Town> getTownPage(UserDetailsImpl userDetails, StatisticalPageDto statisticalPageDto, Pageable page) {
        Page<Town> townPage = townRepository.findAll((Specification<Town>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            //镇名称
            if (StringUtils.isNotEmpty(statisticalPageDto.getName())) {
                predicates.add(cb.like(root.get("name").as(String.class), "%" + statisticalPageDto.getName() + "%"));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        },page);
        return townPage;
    }
}