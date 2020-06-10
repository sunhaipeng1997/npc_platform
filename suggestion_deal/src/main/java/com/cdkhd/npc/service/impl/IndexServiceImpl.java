package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.HomePageVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
import com.cdkhd.npc.repository.base.GovernmentUserRepository;
import com.cdkhd.npc.repository.member_house.SuggestionBusinessRepository;
import com.cdkhd.npc.repository.member_house.SuggestionRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitRepository;
import com.cdkhd.npc.repository.suggestion_deal.UnitSuggestionRepository;
import com.cdkhd.npc.service.IndexService;
import com.cdkhd.npc.vo.RespBody;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.*;

import static com.cdkhd.npc.util.SysUtil.getLast12Month;


@Service
public class IndexServiceImpl implements IndexService {

    private UnitRepository unitRepository;

    private SuggestionBusinessRepository suggestionBusinessRepository;

    private GovernmentUserRepository governmentUserRepository;

    private SuggestionRepository suggestionRepository;

    private UnitSuggestionRepository unitSuggestionRepository;

    @Autowired
    public IndexServiceImpl(UnitRepository unitRepository, SuggestionBusinessRepository suggestionBusinessRepository, GovernmentUserRepository governmentUserRepository, SuggestionRepository suggestionRepository, UnitSuggestionRepository unitSuggestionRepository) {
        this.unitRepository = unitRepository;
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.governmentUserRepository = governmentUserRepository;
        this.suggestionRepository = suggestionRepository;
        this.unitSuggestionRepository = unitSuggestionRepository;
    }

    @Override
    public RespBody getSugNumber(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        Integer newSug = 0;
        Integer completedSug = 0;
        Integer dealingSug = 0;
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date monthDate = cal.getTime();
        if(userDetails.getLevel().equals(LevelEnum.TOWN.getValue())){
            newSug = suggestionRepository.countTownMonthNewNumber(monthDate,LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());//本月新审核过的
            completedSug = suggestionRepository.countTownMonthCompletedNumber(monthDate,LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());//本月完成的
            dealingSug = suggestionRepository.countTownDealingNumber(LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());//办理中的
        }else{
            newSug = suggestionRepository.countAreaMonthNewNumber(monthDate,LevelEnum.AREA.getValue(), userDetails.getArea().getUid());//本月新审核过的
            completedSug = suggestionRepository.countAreaMonthCompletedNumber(monthDate,LevelEnum.AREA.getValue(), userDetails.getArea().getUid());//本月完成的
            dealingSug = suggestionRepository.countAreaDealingNumber(LevelEnum.AREA.getValue(), userDetails.getArea().getUid());//办理中的
        }
        HomePageVo homePageVo = new HomePageVo();
        homePageVo.setNewSug(newSug);
        homePageVo.setCompletedSug(completedSug);
        homePageVo.setDealingSug(dealingSug);
        body.setData(homePageVo);
        return body;
    }

    @Override
    public RespBody getSugCount(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        ArrayList<Date> last12Month = getLast12Month();
        ArrayList<Date> allDate = new ArrayList<>(13);
        allDate.add(DateUtils.addMonths(last12Month.get(0), 1));
        Collections.reverse(last12Month);
        allDate.addAll(last12Month);
        Collections.reverse(allDate);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        int index = 0;
        for (Date date : last12Month) {
            Date startAt = allDate.get(index);
            Date endAt = allDate.get(++index);
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

    private List<Suggestion> getSuggestions(UserDetailsImpl userDetails, Date startAt, Date endAt) {
        List<Suggestion> suggestionList = suggestionRepository.findAll((Specification<Suggestion>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            predicates.add(cb.greaterThanOrEqualTo(root.get("auditTime").as(Date.class), startAt));
            predicates.add(cb.lessThanOrEqualTo(root.get("auditTime").as(Date.class), endAt));
            predicates.add(cb.equal(root.get("level").as(Byte.class), userDetails.getLevel()));
            predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
            }
            predicates.add(cb.greaterThanOrEqualTo(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue()));//建议状态在提交到政府以后
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return suggestionList;
    }

    private List<SuggestionBusiness> getSuggestionBusiness(UserDetailsImpl userDetails) {
        List<SuggestionBusiness> suggestionBusinesses = suggestionBusinessRepository.findAll((Specification<SuggestionBusiness>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                //这里需要判断是不是街道，
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                if (userDetails.getTown().getType() == (byte)1) {
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                }else{
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));//街道的话，用区上的类型
                }
            }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
            }
            predicates.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));//建议状态在提交到政府以后
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return suggestionBusinesses;
    }

    @Override
    public RespBody sugBusinessLine(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> suggestionBusinesses = this.getSuggestionBusiness(userDetails);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        for (SuggestionBusiness suggestionBusiness : suggestionBusinesses) {
            List<Suggestion> allSugs = suggestionRepository.findBySuggestionBusinessUid(suggestionBusiness.getUid());
            xaxis.add(suggestionBusiness.getName());
            yaxis.add(allSugs.size());
        }
        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        body.setData(data);
        return body;
    }

    private List<Unit> getUnitList(UserDetailsImpl userDetails) {
        List<Unit> unitList = unitRepository.findAll((Specification<Unit>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDel").as(Boolean.class)));
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                //这里需要判断是不是街道，
                predicates.add(cb.equal(root.get("town").get("uid").as(String.class), userDetails.getTown().getUid()));
                if (userDetails.getTown().getType() == (byte)1) {
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                }else{
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));//街道的话，用区上的类型
                }
            }else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())){
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
            }
            predicates.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));//建议状态在提交到政府以后
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return unitList;
    }

    @Override
    public RespBody sugUnitDealingLine(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<Unit> unitList = this.getUnitList(userDetails);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        for (Unit unit : unitList) {
            List<Suggestion> allSugs = unitSuggestionRepository.findDealingSuggestionByUnitUid(unit.getUid());
            xaxis.add(unit.getName());
            yaxis.add(allSugs.size());
        }
        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        body.setData(data);
        return body;
    }

    @Override
    public RespBody sugUnitCompletedLine(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<Unit> unitList = this.getUnitList(userDetails);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        for (Unit unit : unitList) {
            List<Suggestion> allSugs = unitSuggestionRepository.findCompletedSuggestionByUnitUid(unit.getUid());
            xaxis.add(unit.getName());
            yaxis.add(allSugs.size());
        }
        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        body.setData(data);
        return body;
    }


}
