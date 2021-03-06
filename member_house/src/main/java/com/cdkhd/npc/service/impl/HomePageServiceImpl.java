package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.HomePageVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.PerformanceStatusEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.member_house.OpinionRepository;
import com.cdkhd.npc.repository.member_house.PerformanceRepository;
import com.cdkhd.npc.repository.member_house.PerformanceTypeRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.service.HomePageService;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.cdkhd.npc.util.SysUtil.getLast12Month;

@Service
@Transactional
public class HomePageServiceImpl implements HomePageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HomePageServiceImpl.class);

    private PerformanceRepository performanceRepository;

    private PerformanceTypeRepository performanceTypeRepository;

    private SuggestionRepository suggestionRepository;

    private OpinionRepository opinionRepository;

    private NpcMemberRepository npcMemberRepository;

    @Autowired
    public HomePageServiceImpl(PerformanceRepository performanceRepository, PerformanceTypeRepository performanceTypeRepository, SuggestionRepository suggestionRepository, OpinionRepository opinionRepository, NpcMemberRepository npcMemberRepository) {
        this.performanceRepository = performanceRepository;
        this.performanceTypeRepository = performanceTypeRepository;
        this.suggestionRepository = suggestionRepository;
        this.opinionRepository = opinionRepository;
        this.npcMemberRepository = npcMemberRepository;
    }

    @Override
    public RespBody getTodayNumber(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        Integer suggestion = 0;
        Integer opinion = 0;
        Integer performance = 0;
        LocalDate todayLocal = LocalDate.now();//????????????/  yyyy-MM-dd  java8 ???????????????
        Date today = Date.from(todayLocal.atStartOfDay(ZoneOffset.ofHours(8)).toInstant());
        if(userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
            suggestion = suggestionRepository.countTownTodayNumber(today,userDetails.getLevel(), userDetails.getTown().getUid());
            opinion = opinionRepository.countTownTodayNumber(today,userDetails.getLevel(), userDetails.getTown().getUid());
            performance = performanceRepository.countTownTodayNumber(today,userDetails.getLevel(), userDetails.getTown().getUid());
        }else{
            List<NpcMember> npcMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),userDetails.getLevel());
            List<String> mobiles = npcMembers.stream().map(NpcMember::getMobile).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(mobiles)) {
                suggestion = suggestionRepository.countAreaTodayNumber(today, mobiles, userDetails.getArea().getUid());
                opinion = opinionRepository.countAreaTodayNumber(today, mobiles, userDetails.getArea().getUid());
                performance = performanceRepository.countAreaTodayNumber(today, mobiles, userDetails.getArea().getUid());
            }
        }
        HomePageVo homePageVo = new HomePageVo();
        homePageVo.setOpinion(opinion);
        homePageVo.setPerformance(performance);
        homePageVo.setSuggestion(suggestion);
        body.setData(homePageVo);
        return body;
    }

    @Override
    public RespBody drawSuggestion(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        ArrayList<Date> last12Month = getLast12Month();
        ArrayList<Date> allDate = new ArrayList<>(13);
        allDate.add(DateUtils.addMonths(last12Month.get(0), 1));
        allDate.addAll(last12Month);
        Collections.reverse(last12Month);
        Collections.reverse(allDate);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        int index = 0;
        for (Date date : last12Month) {
            Date startAt = allDate.get(index);
            Date endAt = allDate.get(++index);
//            List<Suggestion> allSugs = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> cb.and(cb.greaterThanOrEqualTo(root.get("sendAt").as(Date.class), startAt), cb.lessThan(root.get("sendAt").as(Date.class), endAt)));
            List<Suggestion> allSugs = this.getSuggestions(userDetails,startAt,endAt);
            xaxis.add(DateFormatUtils.format(date, "yyyy-MM"));
            yaxis.add(allSugs.size());
        }

        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        body.setData(data);
        return body;
    }

    @Override
    public RespBody drawOpinion(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        ArrayList<Date> last12Month = getLast12Month();
        ArrayList<Date> allDate = new ArrayList<>(13);
        allDate.add(DateUtils.addMonths(last12Month.get(0), 1));
        allDate.addAll(last12Month);
        Collections.reverse(last12Month);
        Collections.reverse(allDate);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        int index = 0;
        for (Date date : last12Month) {
            Date startAt = allDate.get(index);
            Date endAt = allDate.get(++index);
//            List<Suggestion> all
// Sugs = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> cb.and(cb.greaterThanOrEqualTo(root.get("sendAt").as(Date.class), startAt), cb.lessThan(root.get("sendAt").as(Date.class), endAt)));
            List<Opinion> allOpin = this.getOpinions(userDetails,startAt,endAt);
            xaxis.add(DateFormatUtils.format(date, "yyyy-MM"));
            yaxis.add(allOpin.size());
        }

        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        body.setData(data);
        return body;
    }

    @Override
    public RespBody drawPerformance(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        ArrayList<Date> last12Month = getLast12Month();
        ArrayList<Date> allDate = new ArrayList<>(13);
        allDate.add(DateUtils.addMonths(last12Month.get(0), 1));
        allDate.addAll(last12Month);
        Collections.reverse(last12Month);
        Collections.reverse(allDate);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        int index = 0;
        for (Date date : last12Month) {
            Date startAt = allDate.get(index);
            Date endAt = allDate.get(++index);
            List<Performance> allPers = this.getPerformance(userDetails,startAt,endAt);
            xaxis.add(DateFormatUtils.format(date, "yyyy-MM"));
            yaxis.add(allPers.size());
        }


        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        body.setData(data);
        return body;
    }

    @Override
    public RespBody drawPerformanceType(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<PerformanceType> performanceTypes = this.getPerformanceTypes(userDetails);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        for (PerformanceType performanceType : performanceTypes) {
            List<Performance> allPers;
            if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                allPers = performanceRepository.findByPerformanceTypeUidAndLevelAndAreaUidAndIsDelFalse(performanceType.getUid(), LevelEnum.AREA.getValue(), userDetails.getArea().getUid());
            } else{
                allPers = performanceRepository.findByPerformanceTypeUidAndLevelAndTownUidAndIsDelFalse(performanceType.getUid(), LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());
            }
            xaxis.add(performanceType.getName());
            yaxis.add(allPers.size());
        }

        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        body.setData(data);
        return body;
    }


    private List<Suggestion> getSuggestions(UserDetailsImpl userDetails, Date startAt, Date endAt) {
        List<Suggestion> suggestionList = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startAt));
            predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), endAt));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));//???????????????????????????
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                //?????????????????????????????????????????????????????????????????????????????????????????????
                List<NpcMember> npcMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),userDetails.getLevel());
                if (CollectionUtils.isNotEmpty(npcMembers)) {
                    List<String> mobiles = npcMembers.stream().map(NpcMember::getMobile).collect(Collectors.toList());
                    //???????????????????????????
                    Expression<String> exp = root.get("raiser").get("mobile").as(String.class);
                    predicates.add(exp.in(mobiles));//????????????????????????????????????
                }
            }
            predicates.add(cb.greaterThanOrEqualTo(root.get("status").as(Byte.class), (byte) 3));//todo ????????????????????????????????????
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return suggestionList;
    }

    private List<Opinion> getOpinions(UserDetailsImpl userDetails, Date startAt, Date endAt){
        List<Opinion> opinionList = opinionRepository.findAll((Specification<Opinion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startAt));
            predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), endAt));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                //?????????????????????????????????????????????????????????????????????????????????????????????
                List<NpcMember> npcMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),userDetails.getLevel());
                if (CollectionUtils.isNotEmpty(npcMembers)) {
                    List<String> mobiles = npcMembers.stream().map(NpcMember::getMobile).collect(Collectors.toList());
                    //???????????????????????????
                    Expression<String> exp = root.get("receiver").get("mobile").as(String.class);
                    predicates.add(exp.in(mobiles));//????????????????????????????????????
                }
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return opinionList;
    }

    private List<PerformanceType> getPerformanceTypes(UserDetailsImpl userDetails){
        List<PerformanceType> performanceTypes = performanceTypeRepository.findAll((Specification<PerformanceType>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                if (userDetails.getTown().getType() == (byte)1) {
                    predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                }else if (userDetails.getTown().getType() == (byte)2) {
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
                }
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return performanceTypes;
    }

    private List<Performance> getPerformance(UserDetailsImpl userDetails, Date startAt, Date endAt){
        List<Performance> performances = performanceRepository.findAll((Specification<Performance>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime").as(Date.class), startAt));
            predicates.add(cb.lessThanOrEqualTo(root.get("createTime").as(Date.class), endAt));
            predicates.add(cb.equal(root.get("status").as(Byte.class), PerformanceStatusEnum.AUDIT_SUCCESS.getValue()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                //?????????????????????????????????????????????????????????????????????????????????????????????
                List<NpcMember> npcMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),userDetails.getLevel());
                if (CollectionUtils.isNotEmpty(npcMembers)) {
                    List<String> mobiles = npcMembers.stream().map(NpcMember::getMobile).collect(Collectors.toList());
                    //???????????????????????????
                    Expression<String> exp = root.get("npcMember").get("mobile").as(String.class);
                    predicates.add(exp.in(mobiles));//????????????????????????????????????
                }
            }
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return performances;
    }
}
