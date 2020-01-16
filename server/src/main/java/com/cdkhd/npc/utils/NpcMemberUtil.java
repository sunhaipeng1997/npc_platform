package com.cdkhd.npc.utils;

import com.cdkhd.npc.entity.NpcMember;

import java.util.Set;

//判断当前登录账号的身份
public class NpcMemberUtil {

    public static NpcMember getCurrentIden(Byte level, Set<NpcMember> npcMembers) {
        NpcMember member = null;
        for (NpcMember npcMember : npcMembers) {
            if (npcMember.getLevel().equals(level)) {
                member = npcMember;
            }
        }
        return member;
    }
}
