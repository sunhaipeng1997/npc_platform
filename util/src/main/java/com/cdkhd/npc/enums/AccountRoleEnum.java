package com.cdkhd.npc.enums;

public enum AccountRoleEnum {
    VOTER("选民"),
    NPC_MEMBER("代表"),
    GOVERNMENT("政府"),
    UNIT("办理单位"),
    BACKGROUND_ADMIN("后台管理员");

    private String name;

    AccountRoleEnum(String name) {
        this.name = name;
    }

    public String getKeyword() {
        return this.name();
    }

    public String getName() {
        return this.name;
    }
}
