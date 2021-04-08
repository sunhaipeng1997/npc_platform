package com.cdkhd.npc.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cdkhd.npc.component.UserDetailsImpl;
import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.enums.AccountRoleEnum;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.enums.NpcMemberRoleEnum;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.SpecialFunctionService;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rfx
 * 2020-02-26 10:41
 */
@Service
@Transactional
public class SpecialFunctionServiceImpl implements SpecialFunctionService {

    private NpcMemberRepository npcMemberRepository;

    private NpcMemberRoleRepository npcMemberRoleRepository;

    private NpcMemberGroupRepository npcMemberGroupRepository;

    private TownRepository townRepository;

    private SystemSettingRepository systemSettingRepository;

    @Autowired
    public SpecialFunctionServiceImpl(NpcMemberRepository npcMemberRepository, NpcMemberRoleRepository npcMemberRoleRepository, NpcMemberGroupRepository npcMemberGroupRepository, TownRepository townRepository, SystemSettingRepository systemSettingRepository) {
        this.npcMemberRepository = npcMemberRepository;
        this.npcMemberRoleRepository = npcMemberRoleRepository;
        this.npcMemberGroupRepository = npcMemberGroupRepository;
        this.townRepository = townRepository;
        this.systemSettingRepository = systemSettingRepository;
    }



    @Override
    public RespBody getSettings(UserDetailsImpl userDetails) {
        RespBody<JSONObject> body = new RespBody<>();
        JSONObject obj = new JSONObject();
        // 新闻审核人员
        List<String[]> array = Lists.newArrayList();
        // NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword()
        List<NpcMember> npcMembers = this.getMembers(NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword(),userDetails);
        constructItemList(array, npcMembers, userDetails.getLevel());
        obj.put(NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword(), array);

        // 通知公告审核人员
        // NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword()
        array = Lists.newArrayList();
        npcMembers = this.getMembers(NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword(),userDetails);
        constructItemList(array, npcMembers, userDetails.getLevel());
        obj.put(NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword(), array);

        // 建议接收人员
        array = Lists.newArrayList();
        npcMembers = this.getMembers(NpcMemberRoleEnum.SUGGESTION_RECEIVER.getKeyword(),userDetails);
        constructItemList(array, npcMembers, userDetails.getLevel());
        obj.put(NpcMemberRoleEnum.SUGGESTION_RECEIVER.getKeyword(), array);


        // 代表履职总的审核人
        array = Lists.newArrayList();
        NpcMemberRole performanceManager = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword());
        if (performanceManager == null) {
            body.setMessage("找不到指定的角色");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        List<NpcMember> npcMemberList = new ArrayList<>();
        for (NpcMember performanceAuditors : performanceManager.getNpcMembers()) {//获取所有的履职总审核
            if (performanceAuditors != null && performanceAuditors.getStatus().equals(StatusEnum.ENABLED.getValue()) && !performanceAuditors.getIsDel()) {
                if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && performanceAuditors.getTown().getUid().equals(userDetails.getTown().getUid()) && performanceAuditors.getLevel().equals(LevelEnum.TOWN.getValue())) {//镇上管理员设置镇上的特殊职能、把区上的过滤掉
                    npcMemberList.add(performanceAuditors);
                } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) && performanceAuditors.getArea().getUid().equals(userDetails.getArea().getUid()) && performanceAuditors.getLevel().equals(LevelEnum.AREA.getValue())) {
                    npcMemberList.add(performanceAuditors);
                }
            }
        }
        constructItemList(array, npcMemberList, userDetails.getLevel());
        obj.put(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword(), array);

        //小组审核人
        List<JSONObject> glist = new ArrayList<>();
        if (userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            List<Town> townList = townRepository.findByAreaUidAndStatusAndIsDelFalseOrderByNameAsc(userDetails.getArea().getUid(),StatusEnum.ENABLED.getValue());
            for (Town town : townList) {
                JSONObject gobj = new JSONObject();
                gobj.put("UID", town.getUid());
                gobj.put("NAME", town.getName());
                Set<NpcMember> members = town.getNpcMembers();
                for (NpcMember member : members) {
                    if (member.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toSet()).contains(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword()) && member.getStatus().equals(StatusEnum.ENABLED.getValue()) && !member.getIsDel() && member.getArea().getUid().equals(userDetails.getArea().getUid())) {
                        gobj.put("AUDITOR_UID", member.getUid());
                        gobj.put("AUDITOR_NAME", member.getName());
                    }
                }
                glist.add(gobj);
            }
            obj.put("GROUPS", glist);
        } else if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            Set<NpcMemberGroup> groups = userDetails.getTown().getNpcMemberGroups();
            List<NpcMemberGroup> groupList = Lists.newArrayList(groups);
            groupList.sort(Comparator.comparing(NpcMemberGroup::getName));
            for (NpcMemberGroup group : groupList) {
                JSONObject gobj = new JSONObject();
                gobj.put("UID", group.getUid());
                gobj.put("NAME", group.getName());
                Set<NpcMember> members = group.getMembers();
                for (NpcMember member : members) {
                    if (member.getNpcMemberRoles().stream().map(NpcMemberRole::getKeyword).collect(Collectors.toSet()).contains(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword()) && member.getStatus().equals(StatusEnum.ENABLED.getValue()) && !member.getIsDel()) {
                        gobj.put("AUDITOR_UID", member.getUid());
                        gobj.put("AUDITOR_NAME", member.getName());
                    }
                }
                glist.add(gobj);
            }
            obj.put("GROUPS", glist);
        }
        //小组审核人开关
        SystemSetting systemSetting;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndTownUid(userDetails.getLevel(),userDetails.getTown().getUid());
        }else{
            systemSetting = systemSettingRepository.findByLevelAndAreaUid(userDetails.getLevel(),userDetails.getArea().getUid());
        }
        obj.put("SWITCHS", systemSetting.getPerformanceGroupAudit());
        body.setData(obj);
        return body;
    }

    private List<NpcMember> getMembers(String key, UserDetailsImpl userDetails){
        NpcMemberRole newsAuditor = npcMemberRoleRepository.findByKeyword(key);//新闻审核人角色
        List<NpcMember> npcMemberList = newsAuditor.getNpcMembers().stream().filter(npcMember -> npcMember.getLevel().equals(userDetails.getLevel())).collect(Collectors.toList());
        if (userDetails.getRoles().contains(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword()) && userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {//后台管理员身份才能设置特殊职能
            npcMemberList.removeIf(member -> !member.getTown().getUid().equals(userDetails.getTown().getUid()) && member.getLevel().equals(LevelEnum.TOWN.getValue()) && (member.getStatus().equals(StatusEnum.ENABLED.getValue()) || !member.getIsDel()));
        } else if (userDetails.getRoles().contains(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword()) && userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            npcMemberList.removeIf(member -> !member.getArea().getUid().equals(userDetails.getArea().getUid()) && member.getLevel().equals(LevelEnum.AREA.getValue()) && (member.getStatus().equals(StatusEnum.ENABLED.getValue()) || !member.getIsDel()));
        }
        return npcMemberList;
    }

    //新闻审核
    @Override
    public RespBody newsAuditor(UserDetailsImpl userDetails, List<String> uids) {
        return auditorSetting(userDetails, uids, NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword());
    }



    private RespBody auditorSetting(UserDetailsImpl userDetails, List<String> uids, String roleName) {
        RespBody body = new RespBody();
        if (CollectionUtils.isEmpty(uids)) {
            body.setMessage("不能设置为空");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        SystemSetting systemSetting;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndTownUid(userDetails.getLevel(),userDetails.getTown().getUid());
        }else{
            systemSetting = systemSettingRepository.findByLevelAndAreaUid(userDetails.getLevel(),userDetails.getArea().getUid());
        }
        if (systemSetting.getPerformanceGroupAudit() && roleName.equals(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword())) {//开关关闭的时候，就不验证审核人员
            for (String uid : uids) {
                String memberName = this.validateGroupAuditor(uid, roleName, userDetails.getLevel());
                if (StringUtils.isNotEmpty(memberName)) {
                    body.setMessage(memberName + "已经是审核人员，不能设置为总审核人员！");
                    body.setStatus(HttpStatus.BAD_REQUEST);
                    return body;
                }
            }
        }

        Set<NpcMember> npcMemberList = npcMemberRepository.findByUidIn(uids);
        if (npcMemberList.size() != uids.size()) {
            body.setMessage("找不到指定的代表");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        NpcMemberRole role = npcMemberRoleRepository.findByKeyword(roleName);
        if (role == null) {
            body.setMessage("找不到指定的角色");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        // 清除之前的人
        List<NpcMember> npcMembers;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            npcMembers = npcMemberRepository.findByTownUidAndLevelAndIsDelFalse(userDetails.getTown().getUid(),userDetails.getLevel());
        } else {
            npcMembers = npcMemberRepository.findByAreaUidAndLevelAndIsDelFalse(userDetails.getArea().getUid(),userDetails.getLevel());
        }

        for (NpcMember member : npcMembers) {
            Set<NpcMemberRole> roles = member.getNpcMemberRoles();
            roles.removeIf(r -> r.getKeyword().equals(roleName));
            member.setNpcMemberRoles(roles);
            npcMemberRepository.saveAndFlush(member);
        }

        // 保存新的
        for (NpcMember npcMember : npcMemberList) {
            Set<NpcMemberRole> roles = npcMember.getNpcMemberRoles();
            roles.add(role);
            npcMember.setNpcMemberRoles(roles);
            npcMemberRepository.saveAndFlush(npcMember);
        }
        return body;
    }

    //验证这个审核人有没有被设置为小组审核人
    private String validateGroupAuditor(String uid, String roleName,Byte level) {
        String memberName = "";
        if (NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword().equals(roleName)) {//设置履职总审核
            NpcMemberRole role = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword());//查询小组履职审核人员
            for (NpcMember npcMember : role.getNpcMembers()) {//判断这个代表有没有被设置为小组审核人
                if (npcMember.getUid().equals(uid) && level.equals(npcMember.getLevel())) {
                    memberName = npcMember.getName();
                }
            }
        }
        if (NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword().equals(roleName)) {
            NpcMemberRole role = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword());//查询镇履职审核人员
            for (NpcMember npcMember : role.getNpcMembers()) {
                if (npcMember.getUid().equals(uid) && level.equals(npcMember.getLevel())) {
                    memberName = npcMember.getName();
                }
            }
        }
        return memberName;
    }


    private Boolean validateGeneralAuditor(String uid){
        Boolean isGeneralAuditor = false;
        NpcMemberRole npcMemberRole = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword());
        Set<NpcMember> npcMemberList = npcMemberRole.getNpcMembers();
        for (NpcMember npcMember : npcMemberList) {
            if (npcMember.getUid().equals(uid)){
                isGeneralAuditor = true;
                break;
            }
        }
        return isGeneralAuditor;
    }

    //将某种管理员角色的代表封装为要返回给前端的数据项[groupUid, uid]，将数据项添加到list中
    private void constructItemList(List<String[]> list, List<NpcMember> memberList, Byte level) {
        //先将所有代表按组分类
        Map<String, List<NpcMember>> map = new HashMap<>();
        for (NpcMember member : memberList) {
            String name;
            if (level.equals(LevelEnum.TOWN.getValue())) {
                name = member.getNpcMemberGroup().getName();
            }else {
                name = member.getTown().getName();
            }
            if (!map.containsKey(name)) {
                List<NpcMember> members = new ArrayList<>();
                members.add(member);
                map.put(name, members);
            } else {
                map.get(name).add(member);
            }
        }

        //将组按组名排序
        List<String> groupNameList = Lists.newArrayList(map.keySet());
        groupNameList.sort(String::compareTo);
        //再组内按id排序并添加到memberList
        List<NpcMember> sortedMemberList = Lists.newArrayList();
        for (String groupName : groupNameList) {
            List<NpcMember> listByGroup = map.get(groupName);
            listByGroup.sort(Comparator.comparing(NpcMember::getId));
            sortedMemberList.addAll(listByGroup);
        }

        //遍历排好序的memberList，按格式构造list
        for (NpcMember member : sortedMemberList) {
            String[] item = new String[2];
            if (level.equals(LevelEnum.TOWN.getValue())) {
                NpcMemberGroup group = member.getNpcMemberGroup();
                item[0] = group.getUid();
            }else {
                Town town = member.getTown();
                item[0] = town.getUid();
            }
            item[1] = member.getUid();
            list.add(item);
        }
    }

}
