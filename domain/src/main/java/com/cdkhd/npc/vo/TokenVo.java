package com.cdkhd.npc.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Setter
@Getter
public class TokenVo {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date signAt;

    private long expireAt;

    private String token;

    //用户名
    private String username;

    //用户角色
    private Set<String> roles;
}
