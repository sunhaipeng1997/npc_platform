package com.cdkhd.npc.entity.dto;

import com.cdkhd.npc.dto.PageDto;

/**
 * @描述 移动端首页
 */
public class MobilePageDto extends PageDto {

    //商品名称
    private String name;

    //类型
    private String type;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
