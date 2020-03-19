package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

/**
 * @Description
 * @Author rfx
 * @Date 2019-12-03
 */

@Setter
@Getter
@ToString
@Entity
@Table(name = "login_wechat")
public class LoginWeChat extends BaseDomain {

    @Column(name = "open_id")
    private String openId;

    /**
     * 微信号
     */
    @Column(name = "weichat")
    private String weichat;

    /**
     * 账号表id
     */
    @OneToOne(targetEntity = Account.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account", referencedColumnName = "id")
    private Account account;

}
