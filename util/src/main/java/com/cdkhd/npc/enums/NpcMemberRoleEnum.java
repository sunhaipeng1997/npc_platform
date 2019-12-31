package com.cdkhd.npc.enums;

public enum NpcMemberRoleEnum {
    MEMBER("普通代表"),
    CHAIRMAN("人大主席"),
    SPECIAL_MAN("特殊人员");

    private String name;

    NpcMemberRoleEnum(String name) {
        this.name = name;
    }

    public String getKeyword() {
        return this.name();
    }

    public String getName() {
        return name;
    }
}
