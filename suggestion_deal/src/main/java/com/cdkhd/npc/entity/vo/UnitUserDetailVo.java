package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.UnitUser;
import com.cdkhd.npc.enums.GenderEnum;
import com.cdkhd.npc.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitUserDetailVo {

    private String name;

    private String username;

    private String mobile;

    private String code;

    private Byte gender;
    private String genderName;

    private String statusName;

    private String comment;

    public static UnitUserDetailVo convert(UnitUser unitUser) {
        UnitUserDetailVo vo = new UnitUserDetailVo();
        vo.setComment(unitUser.getComment());
        vo.setGender(unitUser.getGender());
        vo.setGenderName(GenderEnum.getName(unitUser.getGender()));
        vo.setMobile(unitUser.getMobile());
        vo.setName(unitUser.getName());
        vo.setUsername(unitUser.getName());
        vo.setStatusName(StatusEnum.getName(unitUser.getStatus()));
        return vo;
    }

}
