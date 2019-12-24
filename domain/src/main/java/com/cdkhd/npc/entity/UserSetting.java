package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author  rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table ( name ="user_setting" )
public class UserSetting extends BaseDomain {

    //  a.扫码签到  b.提建议 c.提意见(选民) d.履职登记
    @Column(name = "quick_work_type" )
	private Byte quickWorkType;


    /**
     * 账号表id
     */
    @OneToOne(targetEntity=Account.class, fetch = FetchType.LAZY)
    private Account account;


}
