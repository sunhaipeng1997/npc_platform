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

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "open_id")
    private String openId;

    /**
     * 开放平台下的unionId
     */
    @Column(name = "union_id")
    private String unionId;

    /**
     * 微信号
     */
    @Column(name = "wechat_id")
    private String wechatId;

    /**
     * 账号表id
     */
    @OneToOne(targetEntity = Account.class, fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "account", referencedColumnName = "id")
    private Account account;

}
