package com.cdkhd.npc.enums;

public enum NewsStatusEnum {

    CREATED("已创建"),//初始值
    DRAFT("草稿"),
    UNDER_REVIEW("审核中"),
    NOT_APPROVED("不通过"),
    RELEASABLE("待发布"),
    RELEASED("已发布");

    private String name;

    NewsStatusEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
