package com.cdkhd.npc.entity.vo;

import com.cdkhd.npc.entity.Account;
import com.cdkhd.npc.vo.BaseVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

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
    private Date lastLoginTime;

    //状态
    private Byte status;

    public static AccountVo convert(Account account) {
        AccountVo vo = new AccountVo();
        BeanUtils.copyProperties(account, vo);
        if (account.getVoter() != null){
            vo.setRealname(account.getVoter().getRealname());
            vo.setMobile(account.getVoter().getMobile());
        }
        vo.setLoginTimes(account.getLoginTimes());
        vo.setLastLoginTime(account.getLastLoginTime());
        return vo;
    }
}
