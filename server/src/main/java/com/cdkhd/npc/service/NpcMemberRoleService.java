package com.cdkhd.npc.service;

import com.cdkhd.npc.entity.NpcMember;
import com.cdkhd.npc.vo.RespBody;

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
    List<NpcMember> findByKeyWordAndLevel(String keyword, Byte level);

    /**
     * 通过权限关键字和等级以及详细uid查询代表列表
     * @param keyword 关键字
     * @param level 等级 区、镇
     * @param uid   区：区id；镇：镇id
     * @return
     */
    List<NpcMember> findByKeyWordAndLevelAndUid(String keyword, Byte level, String uid);


    /**
     * 查寻所有小组审核人
     * @param keyword 关键字
     * @param level 等级 区、镇
     * @param uid   小组uid
     * @return
     */
    List<NpcMember> findByKeyWordAndUid(String keyword, Byte level, String uid);

    /**
     * 通过代表uid查询其所有权限
     * @param uid   代表
     * @return
     */
    List<String> findKeyWordByUid(String uid, Boolean isMust);

    /**
     * 查询所有必选的代表角色
     * @return
     */
    RespBody findMustList();

}
