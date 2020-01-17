package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.NpcMemberGroup;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

/**
 * @Description
 * @Author ly
 * @Date 2019-01-10
 */

@Setter
@Getter
public class GroupVo extends BaseVo {

    //小组
    private String name;

    //小组简介
    private String introduction;

    //所属镇名称
    private String townName;

    public static GroupVo convert(NpcMemberGroup npcMemberGroup) {
        GroupVo vo = new GroupVo();
        BeanUtils.copyProperties(npcMemberGroup, vo);
        if(npcMemberGroup.getTown() != null){
            vo.setTownName(npcMemberGroup.getTown().getName());
        }
        return vo;
    }
}
