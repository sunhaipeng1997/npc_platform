package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.entity.NpcMemberRole;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.NpcMemberRepository;
import com.cdkhd.npc.repository.base.NpcMemberRoleRepository;
import com.cdkhd.npc.service.NpcMemberRoleService;
import com.cdkhd.npc.vo.CommonVo;
import com.cdkhd.npc.vo.RespBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NpcMemberRoleServiceImpl implements NpcMemberRoleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMemberRoleServiceImpl.class);

    private NpcMemberRoleRepository npcMemberRoleRepository ;

    private NpcMemberRepository npcMemberRepository ;

    @Autowired
    public NpcMemberRoleServiceImpl(NpcMemberRoleRepository npcMemberRoleRepository, NpcMemberRepository npcMemberRepository) {
        this.npcMemberRoleRepository = npcMemberRoleRepository;
        this.npcMemberRepository = npcMemberRepository;
    }

    @Override
    public List<NpcMember> findByKeyWord(String keyword) {
        NpcMemberRole npcMemberRole = npcMemberRoleRepository.findByKeyword(keyword);
        Set<NpcMember> npcMemberSet = Sets.newHashSet();
        npcMemberSet.addAll(npcMemberRole.getNpcMembers());
        return Lists.newArrayList(npcMemberSet);
    }

    @Override
    public List<NpcMember> findByKeyWordAndLevel(String keyword, Byte level) {
        List<NpcMember> npcMemberList = this.findByKeyWord(keyword);
        List<NpcMember> npcMembers = Lists.newArrayList();
        for (NpcMember npcMember : npcMemberList) {
            if (npcMember.getLevel().equals(level)){
                npcMembers.add(npcMember);
            }
        }
        return npcMembers;
    }

    @Override
    public List<NpcMember> findByKeyWordAndLevelAndUid(String keyword, Byte level, String uid) {
        List<NpcMember> npcMemberList = this.findByKeyWordAndLevel(keyword,level);
        List<NpcMember> npcMembers = Lists.newArrayList();
        for (NpcMember npcMember : npcMemberList) {
            if (level.equals(LevelEnum.TOWN.getValue()) && uid.equals(npcMember.getTown().getUid())){
                npcMembers.add(npcMember);
            }if (level.equals(LevelEnum.AREA.getValue()) && uid.equals(npcMember.getArea().getUid())){
                npcMembers.add(npcMember);
            }
        }
        return npcMembers;
    }

    @Override
    public List<NpcMember> findByKeyWordAndUid(String keyword,Byte level , String uid) {
        List<NpcMember> npcMemberList = this.findByKeyWordAndLevel(keyword,level);
        List<NpcMember> npcMembers = Lists.newArrayList();
        for (NpcMember npcMember : npcMemberList) {
            if (level.equals(LevelEnum.TOWN.getValue()) && npcMember.getNpcMemberGroup() != null && uid.equals(npcMember.getNpcMemberGroup().getUid())){
                npcMembers.add(npcMember);
            }if (level.equals(LevelEnum.AREA.getValue()) && npcMember.getTown() != null && uid.equals(npcMember.getTown().getUid())){
                npcMembers.add(npcMember);
            }
        }
        return npcMembers;
    }


    @Override
    public List<String> findKeyWordByUid(String uid) {
        NpcMember npcMember = npcMemberRepository.findByUid(uid);
        List<String> permissionList = Lists.newArrayList();
        for (NpcMemberRole npcMemberRole : npcMember.getNpcMemberRoles()) {
                permissionList.add(npcMemberRole.getKeyword());
        }
        return permissionList;
    }

    @Override
    public List<String> findKeyWordByUid(String uid, Boolean isMust) {
        NpcMember npcMember = npcMemberRepository.findByUid(uid);
        List<String> permissionList = Lists.newArrayList();
        for (NpcMemberRole npcMemberRole : npcMember.getNpcMemberRoles()) {
            if (isMust.equals(npcMemberRole.getIsMust())) {
                permissionList.add(npcMemberRole.getKeyword());
            }
        }
        return permissionList;
    }

    @Override
    public RespBody findMustList() {
        RespBody body = new RespBody();
        List<NpcMemberRole> npcMemberRoleList = npcMemberRoleRepository.findByIsMustTrue();
        List<CommonVo> commonVos = npcMemberRoleList.stream().map(role -> CommonVo.convert(role.getUid(),role.getName())).collect(Collectors.toList());
        body.setData(commonVos);
        return body;
    }
}
