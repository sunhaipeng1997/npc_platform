package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.entity.vo.AdminHomePageVo;
import com.cdkhd.npc.entity.vo.HomePageVo;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.enums.SuggestionStatusEnum;
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
import java.util.stream.Collectors;

import static com.cdkhd.npc.util.SysUtil.getLast12Month;

@Service
public class IndexServiceImpl implements IndexService {

    private UnitRepository unitRepository;

    private SuggestionBusinessRepository suggestionBusinessRepository;

    private SuggestionRepository suggestionRepository;

    private UnitSuggestionRepository unitSuggestionRepository;

    @Autowired
    public IndexServiceImpl(UnitRepository unitRepository, SuggestionBusinessRepository suggestionBusinessRepository, SuggestionRepository suggestionRepository, UnitSuggestionRepository unitSuggestionRepository) {
        this.unitRepository = unitRepository;
        this.suggestionBusinessRepository = suggestionBusinessRepository;
        this.suggestionRepository = suggestionRepository;
        this.unitSuggestionRepository = unitSuggestionRepository;
    }

    @Override
    public RespBody getGovSugNumber(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        Integer newSug = 0;
        Integer completedSug = 0;
        Integer dealingSug = 0;
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date monthDate = cal.getTime();
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            newSug = suggestionRepository.countTownMonthNewNumber(monthDate, LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());//本月新审核过的
            completedSug = suggestionRepository.countTownMonthCompletedNumber(monthDate, LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());//本月完成的
            dealingSug = suggestionRepository.countTownDealingNumber(LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());//办理中的
        } else {
            newSug = suggestionRepository.countAreaMonthNewNumber(monthDate, LevelEnum.AREA.getValue(), userDetails.getArea().getUid());//本月新审核过的
            completedSug = suggestionRepository.countAreaMonthCompletedNumber(monthDate, LevelEnum.AREA.getValue(), userDetails.getArea().getUid());//本月完成的
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
    public RespBody getGovSugCount(UserDetailsImpl userDetails) {
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
            List<Suggestion> allSugs = this.getSuggestions(userDetails, startAt, endAt);
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
            Predicate submittedGovernment = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.SUBMITTED_GOVERNMENT.getValue());//待转交
            Predicate transferredUnit = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.TRANSFERRED_UNIT.getValue());//单位待接收
            Predicate handling = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLING.getValue());//办理中
            Predicate handled = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.HANDLED.getValue());//办理完成
            Predicate accomplished = cb.equal(root.get("status").as(Byte.class), SuggestionStatusEnum.ACCOMPLISHED.getValue());//办结
            Predicate or = cb.or(submittedGovernment,transferredUnit,handling,handled,accomplished);
            predicates.add(or);//过滤建议状态
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
                if (userDetails.getTown().getType() == (byte) 1) {
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                } else {
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));//街道的话，用区上的类型
                }
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
            }
            predicates.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));//建议状态在提交到政府以后
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return suggestionBusinesses;
    }

    @Override
    public RespBody sugGovBusinessLine(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> suggestionBusinesses = this.getSuggestionBusiness(userDetails);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        for (SuggestionBusiness suggestionBusiness : suggestionBusinesses) {
            List<Suggestion> allSugs = suggestionRepository.findBySuggestionBusinessUid(suggestionBusiness.getUid()).stream().filter(sug -> sug.getStatus().equals(SuggestionStatusEnum.TRANSFERRED_UNIT.getValue()) || sug.getStatus().equals(SuggestionStatusEnum.HANDLING.getValue()) || sug.getStatus().equals(SuggestionStatusEnum.HANDLED.getValue())).collect(Collectors.toList());
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
                if (userDetails.getTown().getType() == (byte) 1) {
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.TOWN.getValue()));
                } else {
                    predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));//街道的话，用区上的类型
                }
            } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
                predicates.add(cb.equal(root.get("area").get("uid").as(String.class), userDetails.getArea().getUid()));
                predicates.add(cb.equal(root.get("level").as(Byte.class), LevelEnum.AREA.getValue()));
            }
            predicates.add(cb.equal(root.get("status").as(Byte.class), StatusEnum.ENABLED.getValue()));//建议状态在提交到政府以后
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
        return unitList;
    }

    @Override
    public RespBody sugGovUnitDealingLine(UserDetailsImpl userDetails) {
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
    public RespBody sugGovUnitCompletedLine(UserDetailsImpl userDetails) {
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

    @Override
    public RespBody adminGetSugNumber(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();

        Integer newSug;  //本月代表新增建议
//        Integer auditPassSug;  //本月审核通过的建议
//        Integer auditRefuseSug;  //本月审核不通过的建议

        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0);
        Date start = cal.getTime();
        Date end = DateUtils.addMonths(start, 1);

        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {  //镇管理员
            //新增的建议不包括status == 0(撤回)和1(草稿箱)的建议
            newSug = suggestionRepository.adminCountTownMonthNewNumber(start, end, LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());
//            auditPassSug = suggestionRepository.adminCountTownMonthAuditPassNumber(monthDate, LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());
//            auditRefuseSug = suggestionRepository.adminCountTownAuditRefuseNumber(monthDate, LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());
        } else {  //区管理员
            newSug = suggestionRepository.adminCountAreaMonthNewNumber(start, end, LevelEnum.AREA.getValue(), userDetails.getArea().getUid());
//            auditPassSug = suggestionRepository.adminCountAreaMonthAuditPassNumber(monthDate, LevelEnum.AREA.getValue(), userDetails.getArea().getUid());
//            auditRefuseSug = suggestionRepository.adminCountAreaAuditRefuseNumber(monthDate, LevelEnum.AREA.getValue(), userDetails.getArea().getUid());
        }

        AdminHomePageVo adminHomePageVo = new AdminHomePageVo();
        adminHomePageVo.setNewSug(newSug);
//        adminHomePageVo.setAuditPassSug(auditPassSug);
//        adminHomePageVo.setAuditRefuseSug(auditRefuseSug);
        body.setData(adminHomePageVo);
        return body;
    }

    @Override
    public RespBody adminNewSugNum(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        ArrayList<Date> last12Month = getLast12Month();
        Collections.reverse(last12Month);
        ArrayList<Date> allDate = new ArrayList<>(13);
        allDate.addAll(last12Month);
        allDate.add(DateUtils.addMonths(last12Month.get(11), 1));
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        int index = 0;
        for (Date date : last12Month) {
            Date startAt = allDate.get(index++);
            Date endAt = allDate.get(index);
            int num = 0;
            if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
                num = suggestionRepository.adminCountTownMonthNewNumber(startAt, endAt, LevelEnum.TOWN.getValue(), userDetails.getTown().getUid());
            }else {
                num = suggestionRepository.adminCountAreaMonthNewNumber(startAt, endAt, LevelEnum.AREA.getValue(), userDetails.getArea().getUid());
            }
            xaxis.add(DateFormatUtils.format(date, "yyyy-MM"));
            yaxis.add(num);
        }
        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        body.setData(data);
        return body;
    }

    @Override
    public RespBody adminSugBusinessLine(UserDetailsImpl userDetails) {
        RespBody body = new RespBody();
        List<SuggestionBusiness> suggestionBusinesses = this.getSuggestionBusiness(userDetails);
        ArrayList<String> xaxis = new ArrayList<>();
        ArrayList<Integer> yaxis = new ArrayList<>();
        for (SuggestionBusiness suggestionBusiness : suggestionBusinesses) {
            List<Suggestion> allSugs = suggestionRepository.findBySuggestionBusinessUidAndStatusGreaterThanAndLevel(suggestionBusiness.getUid(),SuggestionStatusEnum.SUBMITTED_AUDIT.getValue(), userDetails.getLevel());
            xaxis.add(suggestionBusiness.getName());
            yaxis.add(allSugs.size());
        }
        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        body.setData(data);
        return body;
    }

    @Override
    public RespBody adminSugNumGroupBySubordinate(UserDetailsImpl userDetails, Byte type) { //type == 1 :办理中，type == 2 :办结
        RespBody body = new RespBody();
        List<String> xaxis;  //横坐标
        List<Integer> yaxis = new ArrayList<>();  //纵坐标
        Map<String, Integer> map;
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {  //如果是区管理员就按镇分组统计
            Area area = userDetails.getArea();
            Set<Town> towns = area.getTowns();
            if (type == 1) {
                map = towns.stream().collect(Collectors.toMap(Town::getName, town -> countTownDoingSugNum(town)));
            }else {
                map = towns.stream().collect(Collectors.toMap(Town::getName, town -> countTownFinishSugNum(town)));
            }
        }else {  //如果是镇管理员就按小组分组统计（正在办理中的建议）
            Town town = userDetails.getTown();
            Set<NpcMemberGroup> npcMemberGroups = town.getNpcMemberGroups();
            if (type == 1) {
                map = npcMemberGroups.stream().collect(Collectors.toMap(NpcMemberGroup::getName, npcMemberGroup -> countGroupDoingSugNum(npcMemberGroup)));
            }else {
                map = npcMemberGroups.stream().collect(Collectors.toMap(NpcMemberGroup::getName, npcMemberGroup -> countGroupFinishSugNum(npcMemberGroup)));
            }
        }
        xaxis = new ArrayList<>(map.keySet());
        Collections.sort(xaxis);
        for (String name : xaxis) {
            yaxis.add(map.get(name));
        }
        yaxis = xaxis.stream().map(name -> map.get(name)).collect(Collectors.toList());

        String level;
        if (LevelEnum.AREA.getValue().equals(userDetails.getLevel())) {
            level = type == (byte)1 ? "各镇办理中建议总数" : "各镇办结建议总数";
        }else {
            level = type == (byte)1 ? "各小组办理中建议总数" : "各小组办结建议总数";
        }
        JSONObject data = new JSONObject();
        data.put("xaxis", xaxis);
        data.put("yaxis", yaxis);
        data.put("level", level);
        body.setData(data);
        return body;
    }

    /*
    * 查询该镇正在办理中的建议
    * */
    public int countTownDoingSugNum(Town town) {
        return suggestionRepository.countTownDoingSugNum(town.getUid(), LevelEnum.AREA.getValue());
    }

    /*
     * 查询该镇办结的建议
     * */
    public int countTownFinishSugNum(Town town) {
        return suggestionRepository.countTownFinishSugNum(town.getUid(), LevelEnum.AREA.getValue());
    }

    /*
     * 查询该小组正在办理中的建议
     * */
    public int countGroupDoingSugNum(NpcMemberGroup npcMemberGroup) {
        return suggestionRepository.countGroupDoingSugNum(npcMemberGroup.getUid(), LevelEnum.TOWN.getValue());
    }

    /*
     * 查询该小组办结的建议
     * */
    public int countGroupFinishSugNum(NpcMemberGroup npcMemberGroup) {
        return suggestionRepository.countGroupFinishSugNum(npcMemberGroup.getUid(), LevelEnum.TOWN.getValue());
    }
}
