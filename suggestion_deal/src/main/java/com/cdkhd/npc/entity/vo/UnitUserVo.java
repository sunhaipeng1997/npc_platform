package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.UnitUser;
import com.cdkhd.npc.enums.GenderEnum;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class UnitUserVo extends BaseVo {

    //姓名
    private String name;

    //手机号
    private String mobile;

    //性别
    private Byte gender;
    private String genderName;

    //备注
    private String comment;

    //单位uid
    private String unit;
    private String unitName;

    //照片
    private String avatar;

    //登录账号
    private String account;

    public static UnitUserVo convert(UnitUser unitUser) {
        UnitUserVo vo = new UnitUserVo();
        BeanUtils.copyProperties(unitUser,vo);
        vo.setGenderName(GenderEnum.getName(unitUser.getGender()));
        vo.setUnit(unitUser.getUnit().getUid());
        vo.setUnitName(unitUser.getUnit().getName());
        vo.setAccount(unitUser.getAccount().getUsername());
        return vo;
    }

}
