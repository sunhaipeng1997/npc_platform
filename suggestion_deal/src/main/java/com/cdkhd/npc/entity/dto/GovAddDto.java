package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.entity.Government;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description 添加、修改镇的dto
 * @Author  LiYang
 * @Date 2020-05-20
 */
@Getter
@Setter
public class GovAddDto {

    //政府uid
    private String uid;

    // 政府名称
    private String name;

    //管理员账号
    private String account;

    //管理员密码
    private String password;

    //手机号
    private String mobile;

    // 政府介绍
    private String description;

    //地址
    private String address;

    //经度
    private String longitude;

    //纬度
    private String latitude;

    public Government convert() {
        Government government = new Government();
        government.setName(this.getName());
        government.setDescription(this.getDescription());
        return government;
    }

}
