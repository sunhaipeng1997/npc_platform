package com.cdkhd.npc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "t_wechat_access_token")
public class WeChatAccessToken extends BaseDomain {

    private String appid;

    private String accessToken;

}
