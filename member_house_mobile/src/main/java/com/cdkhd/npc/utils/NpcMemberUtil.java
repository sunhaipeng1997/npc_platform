package com.cdkhd.npc.utils;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.util.Constant;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

//判断当前登录账号的身份
public class NpcMemberUtil {

    public static NpcMember getCurrentIden(Byte level, Set<NpcMember> npcMembers) {
        NpcMember member = null;
        for (NpcMember npcMember : npcMembers) {
            if (npcMember.getLevel().equals(level)){
                member = npcMember;
            }
        }
        return member;
    }
}
