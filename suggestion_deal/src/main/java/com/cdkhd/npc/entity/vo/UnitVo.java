package com.cdkhd.npc.entity.vo;


import com.cdkhd.npc.entity.Unit;
import com.cdkhd.npc.enums.StatusEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class UnitVo extends BaseVo {

    //单位名称
    private String name;

    //联系电话
    private String mobile;

    //单位地址
    private String address;

    //经度
    private String longitude;

    //纬度
    private String latitude;

    //单位业务
    private String business;

    private String businessName;

    private String comment;

    private String avatar;

    private Byte status;
    private String statusName;

    private List<UnitUserVo> userVoList;

    public static UnitVo convert(Unit unit) {
        UnitVo unitVo = new UnitVo();
        BeanUtils.copyProperties(unit,unitVo);
        unitVo.setBusiness(unit.getSuggestionBusiness().getUid());
        unitVo.setBusinessName(unit.getSuggestionBusiness().getName());
        unitVo.setStatusName(StatusEnum.getName(unit.getStatus()));
        unitVo.setUserVoList(unit.getUnitUser().stream().map(UnitUserVo::convert).collect(Collectors.toList()));
        return unitVo;
    }

}
