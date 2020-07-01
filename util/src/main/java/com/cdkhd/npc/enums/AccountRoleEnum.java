package com.cdkhd.npc.enums;

public enum AccountRoleEnum {
    VOTER("选民", (byte) 1),
    NPC_MEMBER("代表", (byte) 2),
    GOVERNMENT("政府", (byte) 3),
    UNIT("办理单位", (byte) 4),
    BACKGROUND_ADMIN("后台管理员", (byte) 5);

    private String name;
    private Byte value;

    AccountRoleEnum(String name, Byte value) {
        this.name = name;
        this.value = value;
    }

    public String getKeyword() {
        return this.name();
    }

    public String getName() {
        return this.name;
    }

    public Byte getValue() {
        return this.value;
    }
}
