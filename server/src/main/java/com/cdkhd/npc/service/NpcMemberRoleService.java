package com.cdkhd.npc.service;

import com.cdkhd.npc.entity.NpcMember;

import java.util.List;

public interface NpcMemberRoleService {

    /**
     * 通过权限关键字查询代表列表
     * @param keyword
     * @return
     */
    List<NpcMember> findByKeyWord(String keyword);

    /**
     * 通过权限关键字和等级查询代表列表
     * @param keyword
     * @param level
     * @return
     */
    List<NpcMember> findByKeyWordAndLevel(String keyword,Byte level);

    /**
     * 通过权限关键字和等级以及详细uid查询代表列表
     * @param keyword 关键字
     * @param level 等级 区、镇
     * @param uid   区：区id；镇：镇id
     * @return
     */
    List<NpcMember> findByKeyWordAndLevelAndUid(String keyword,Byte level,String uid);

}