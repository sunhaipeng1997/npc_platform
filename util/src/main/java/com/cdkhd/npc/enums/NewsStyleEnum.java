package com.cdkhd.npc.enums;

public enum NewsStyleEnum {
    CARD("卡片式"),
    LIST("列表式");

    private String name;

    NewsStyleEnum(String name) {
        this.name = name;
    }

    public String getKeyword() {
        return this.name();
    }

    public String getName() {
        return name;
    }
}
