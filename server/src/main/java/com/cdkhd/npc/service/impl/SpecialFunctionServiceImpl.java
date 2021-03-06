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
        // ??????????????????
        List<String[]> array = Lists.newArrayList();
        // NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword()
        List<NpcMember> npcMembers = this.getMembers(NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword(),userDetails);
        constructItemList(array, npcMembers, userDetails.getLevel());
        obj.put(NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword(), array);

        // ????????????????????????
        // NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword()
        array = Lists.newArrayList();
        npcMembers = this.getMembers(NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword(),userDetails);
        constructItemList(array, npcMembers, userDetails.getLevel());
        obj.put(NpcMemberRoleEnum.NOTICE_AUDITOR.getKeyword(), array);

        // ??????????????????
        array = Lists.newArrayList();
        npcMembers = this.getMembers(NpcMemberRoleEnum.SUGGESTION_RECEIVER.getKeyword(),userDetails);
        constructItemList(array, npcMembers, userDetails.getLevel());
        obj.put(NpcMemberRoleEnum.SUGGESTION_RECEIVER.getKeyword(), array);


        // ???????????????????????????
        array = Lists.newArrayList();
        NpcMemberRole performanceManager = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword());
        if (performanceManager == null) {
            body.setMessage("????????????????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        List<NpcMember> npcMemberList = new ArrayList<>();
        for (NpcMember performanceAuditors : performanceManager.getNpcMembers()) {//??????????????????????????????
            if (performanceAuditors != null && performanceAuditors.getStatus().equals(StatusEnum.ENABLED.getValue()) && !performanceAuditors.getIsDel()) {
                if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue()) && performanceAuditors.getTown().getUid().equals(userDetails.getTown().getUid()) && performanceAuditors.getLevel().equals(LevelEnum.TOWN.getValue())) {//??????????????????????????????????????????????????????????????????
                    npcMemberList.add(performanceAuditors);
                } else if (userDetails.getLevel().equals(LevelEnum.AREA.getValue()) && performanceAuditors.getArea().getUid().equals(userDetails.getArea().getUid()) && performanceAuditors.getLevel().equals(LevelEnum.AREA.getValue())) {
                    npcMemberList.add(performanceAuditors);
                }
            }
        }
        constructItemList(array, npcMemberList, userDetails.getLevel());
        obj.put(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword(), array);

        //???????????????
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
        //?????????????????????
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
        NpcMemberRole newsAuditor = npcMemberRoleRepository.findByKeyword(key);//?????????????????????
        List<NpcMember> npcMemberList = newsAuditor.getNpcMembers().stream().filter(npcMember -> npcMember.getLevel().equals(userDetails.getLevel())).collect(Collectors.toList());
        if (userDetails.getRoles().contains(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword()) && userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {//?????????????????????????????????????????????
            npcMemberList.removeIf(member -> !member.getTown().getUid().equals(userDetails.getTown().getUid()) && member.getLevel().equals(LevelEnum.TOWN.getValue()) && (member.getStatus().equals(StatusEnum.ENABLED.getValue()) || !member.getIsDel()));
        } else if (userDetails.getRoles().contains(AccountRoleEnum.BACKGROUND_ADMIN.getKeyword()) && userDetails.getLevel().equals(LevelEnum.AREA.getValue())) {
            npcMemberList.removeIf(member -> !member.getArea().getUid().equals(userDetails.getArea().getUid()) && member.getLevel().equals(LevelEnum.AREA.getValue()) && (member.getStatus().equals(StatusEnum.ENABLED.getValue()) || !member.getIsDel()));
        }
        return npcMemberList;
    }

    //????????????
    @Override
    public RespBody newsAuditor(UserDetailsImpl userDetails, List<String> uids) {
        return auditorSetting(userDetails, uids, NpcMemberRoleEnum.NEWS_AUDITOR.getKeyword());
    }



    private RespBody auditorSetting(UserDetailsImpl userDetails, List<String> uids, String roleName) {
        RespBody body = new RespBody();
        if (CollectionUtils.isEmpty(uids)) {
            body.setMessage("??????????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }
        SystemSetting systemSetting;
        if (userDetails.getLevel().equals(LevelEnum.TOWN.getValue())) {
            systemSetting = systemSettingRepository.findByLevelAndTownUid(userDetails.getLevel(),userDetails.getTown().getUid());
        }else{
            systemSetting = systemSettingRepository.findByLevelAndAreaUid(userDetails.getLevel(),userDetails.getArea().getUid());
        }
        if (systemSetting.getPerformanceGroupAudit() && roleName.equals(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword())) {//????????????????????????????????????????????????
            for (String uid : uids) {
                String memberName = this.validateGroupAuditor(uid, roleName, userDetails.getLevel());
                if (StringUtils.isNotEmpty(memberName)) {
                    body.setMessage(memberName + "?????????????????????????????????????????????????????????");
                    body.setStatus(HttpStatus.BAD_REQUEST);
                    return body;
                }
            }
        }

        Set<NpcMember> npcMemberList = npcMemberRepository.findByUidIn(uids);
        if (npcMemberList.size() != uids.size()) {
            body.setMessage("????????????????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        NpcMemberRole role = npcMemberRoleRepository.findByKeyword(roleName);
        if (role == null) {
            body.setMessage("????????????????????????");
            body.setStatus(HttpStatus.BAD_REQUEST);
            return body;
        }

        // ??????????????????
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

        // ????????????
        for (NpcMember npcMember : npcMemberList) {
            Set<NpcMemberRole> roles = npcMember.getNpcMemberRoles();
            roles.add(role);
            npcMember.setNpcMemberRoles(roles);
            npcMemberRepository.saveAndFlush(npcMember);
        }
        return body;
    }

    //?????????????????????????????????????????????????????????
    private String validateGroupAuditor(String uid, String roleName,Byte level) {
        String memberName = "";
        if (NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword().equals(roleName)) {//?????????????????????
            NpcMemberRole role = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword());//??????????????????????????????
            for (NpcMember npcMember : role.getNpcMembers()) {//??????????????????????????????????????????????????????
                if (npcMember.getUid().equals(uid) && level.equals(npcMember.getLevel())) {
                    memberName = npcMember.getName();
                }
            }
        }
        if (NpcMemberRoleEnum.PERFORMANCE_AUDITOR.getKeyword().equals(roleName)) {
            NpcMemberRole role = npcMemberRoleRepository.findByKeyword(NpcMemberRoleEnum.PERFORMANCE_GENERAL_AUDITOR.getKeyword());//???????????????????????????
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

    //????????????????????????????????????????????????????????????????????????[groupUid, uid]????????????????????????list???
    private void constructItemList(List<String[]> list, List<NpcMember> memberList, Byte level) {
        //??????????????????????????????
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

        //?????????????????????
        List<String> groupNameList = Lists.newArrayList(map.keySet());
        groupNameList.sort(String::compareTo);
        //????????????id??????????????????memberList
        List<NpcMember> sortedMemberList = Lists.newArrayList();
        for (String groupName : groupNameList) {
            List<NpcMember> listByGroup = map.get(groupName);
            listByGroup.sort(Comparator.comparing(NpcMember::getId));
            sortedMemberList.addAll(listByGroup);
        }

        //??????????????????memberList??????????????????list
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
