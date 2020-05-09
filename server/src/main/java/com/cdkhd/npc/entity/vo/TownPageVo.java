package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.BackgroundAdmin;
import com.cdkhd.npc.entity.NpcMemberGroup;
import com.cdkhd.npc.entity.Town;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
public class TownPageVo extends BaseVo {

    // 镇名称
    private String name;

    // 类型
    private Byte type;
    private String typeName;

    //管理员手机号
    private String mobile;

    // 镇介绍
    private String description;

    // 包含小组
    private Set<GroupVo> groups = new HashSet<>();

    public static TownPageVo convert(Town town) {
        TownPageVo vo = new TownPageVo();
        BeanUtils.copyProperties(town, vo);
        vo.setTypeName(town.getType()==1?"镇":"街道");
        Set<NpcMemberGroup> groups = town.getNpcMemberGroups();
        if (groups != null && !groups.isEmpty()) {
            vo.setGroups(groups.stream().map(GroupVo::convert).collect(Collectors.toSet()));
        }
        Set<BackgroundAdmin> backgroundAdmins = town.getBackgroundAdmins();
        if (backgroundAdmins.size() <= 2){
            for(BackgroundAdmin backgroundAdmin : backgroundAdmins){
                if (!backgroundAdmin.getAccount().getLoginUP().getUsername().contains("@cdkhd")){
                    vo.setMobile(backgroundAdmin.getAccount().getMobile());
                }
            }
        }
        return vo;
    }
}
