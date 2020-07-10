package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Unit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitDetailsVo {

    //单位名称
    private String name;

    //联系电话
    private String mobile;

    //单位地址
    private String address;

    //单位业务
    private String business;

    private String businessid;

    private String contactor;

    private String comment;

    private String lng;

    private String lat;

    public static UnitDetailsVo convert(Unit unit) {
        UnitDetailsVo unitDetailsVo = new UnitDetailsVo();

        unitDetailsVo.setName(unit.getName());
        unitDetailsVo.setAddress(unit.getAddress());
        unitDetailsVo.setBusiness(unit.getSuggestionBusiness().getUid());
        //unitDetailsVo.setBusinessid(unit.getBusinessid());
        unitDetailsVo.setMobile(unit.getMobile());
        unitDetailsVo.setComment(unit.getComment());
        unitDetailsVo.setLat(unit.getLatitude());
        unitDetailsVo.setLng(unit.getLongitude());

        return unitDetailsVo;
    }


}
