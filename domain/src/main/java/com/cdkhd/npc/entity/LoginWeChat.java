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

    /**
     * 小程序的 openid
     */
    @Column(name = "wechat_id")
    private String wechatId;

    /**
     * 服务号的 openid
     */
    @Column(name = "open_id")
    private String openId;

    /**
     * 开放平台下的unionId，因为同一个用户在服务号和小程序中有不同的 openid，所以需要绑定同一个 unionId
     * 更多关于 openid 和 unionid 的介绍，参见微信官方文档 https://developers.weixin.qq.com/doc/offiaccount/Getting_Started/Overview.html
     */
    @Column(name = "union_id")
    private String unionId;

    /**
     * 账号表id
     */
    @OneToOne(targetEntity = Account.class, fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "account", referencedColumnName = "id")
    private Account account;

}
