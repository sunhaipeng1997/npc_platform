package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.entity.Town;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TownAddDto {

    //镇uid
    private String uid;

    // 镇名称
    private String name;

    //管理员账号
    private String account;

    //管理员密码
    private String password;

    //手机号
    private String mobile;

    // 镇介绍
    private String description;

    public Town convert() {
        Town town = new Town();
        town.setName(this.getName());
        town.setDescription(this.getDescription());
        return town;
    }
}
