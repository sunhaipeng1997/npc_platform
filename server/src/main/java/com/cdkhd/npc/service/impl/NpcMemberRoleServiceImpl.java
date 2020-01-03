package com.cdkhd.npc.service.impl;

import com.cdkhd.npc.entity.*;
import com.cdkhd.npc.enums.LevelEnum;
import com.cdkhd.npc.repository.base.*;
import com.cdkhd.npc.service.NpcMemberRoleService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class NpcMemberRoleServiceImpl implements NpcMemberRoleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMemberRoleServiceImpl.class);

    private NpcMemberRoleRepository npcMemberRoleRepository ;

    @Autowired
    public NpcMemberRoleServiceImpl(NpcMemberRoleRepository npcMemberRoleRepository) {
        this.npcMemberRoleRepository = npcMemberRoleRepository;
    }

    @Override
    public List<NpcMember> findByKeyWord(String keyword) {
        List<NpcMemberRole> npcMemberRoleList = npcMemberRoleRepository.findByPermissionsKeyword(keyword);
        Set<NpcMember> npcMemberSet = Sets.newHashSet();
        for (NpcMemberRole npcMemberRole : npcMemberRoleList) {
            npcMemberSet.addAll(npcMemberRole.getNpcMembers());
        }
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
}
