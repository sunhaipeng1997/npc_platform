package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.enums.GenderEnum;
import com.cdkhd.npc.vo.BaseVo;
import com.cdkhd.npc.vo.CommonVo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description
 * @Author ly
 * @Date 2019-01-10
 */

@Setter
@Getter
public class AccountVo extends BaseVo {

    //真实姓名
    private String realname;

    //手机号
    private String mobile;

    //登录次数
    private Integer loginTimes;

    //上次登录时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    //状态
    private Byte status;

    //村信息
    private CommonVo village;

    //镇信息
    private CommonVo town;

    //区信息
    private CommonVo area;

    //性别
    private Byte gender;
    private String genderName;

    public static AccountVo convert(Account account) {
        AccountVo vo = new AccountVo();
        BeanUtils.copyProperties(account, vo);
        if (account.getVoter() != null){
            vo.setRealname(account.getVoter().getRealname());
            vo.setMobile(account.getVoter().getMobile());
            vo.setGender(account.getVoter().getGender());
            vo.setGenderName(GenderEnum.getName(account.getVoter().getGender()));
            if (account.getVoter().getVillage() != null) {//没有填写注册信息的用户没有村
                vo.setVillage(CommonVo.convert(account.getVoter().getVillage().getUid(), account.getVoter().getVillage().getName()));
                vo.setTown(CommonVo.convert(account.getVoter().getTown().getUid(), account.getVoter().getTown().getName()));
                vo.setArea(CommonVo.convert(account.getVoter().getArea().getUid(), account.getVoter().getArea().getName()));
            }
        }else{
            vo.setRealname(account.getUsername());
            vo.setMobile(account.getMobile());
        }
        vo.setLoginTimes(account.getLoginTimes());
        vo.setLastLoginTime(account.getLastLoginTime());
        return vo;
    }
}
