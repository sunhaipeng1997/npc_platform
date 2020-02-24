package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Village;
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
public class VillageVo extends BaseVo {

    //村名
    private String name;

    //村简介
    private String introduction;

    //所属小组名称
    private String groupName;

    public static VillageVo convert(Village village) {
        VillageVo vo = new VillageVo();
        BeanUtils.copyProperties(village, vo);
        if(village.getNpcMemberGroup() != null){
            vo.setGroupName(village.getNpcMemberGroup().getName());
        }
        return vo;
    }
}
