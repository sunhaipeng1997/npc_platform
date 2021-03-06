package com.cdkhd.npc.service;

import com.cdkhd.npc.component.MobileUserDetailsImpl;
import com.cdkhd.npc.dto.BaseDto;
import com.cdkhd.npc.entity.dto.LevelDto;
import com.cdkhd.npc.vo.RespBody;

public interface NpcMemberService {

    /**
     * 根据所选的区镇或者小组返回代表列表
     * @param
     * @param level
     * @param uid
     * @return
     */
    RespBody allNpcMembers(Byte level, String uid);

    /**
     * 代表划分情况
     * @params level 等级  区上返回各镇划分 镇上返回各小组划分
     * @return
     */
    RespBody npcMemberUnits(MobileUserDetailsImpl userDetails, Byte level, String uid);

    RespBody relationOfNpcMember(MobileUserDetailsImpl userDetails, LevelDto levelDto);

    RespBody memberUnitDetails(LevelDto levelDto);

    RespBody npcMemberDetails(BaseDto baseDto);
}
