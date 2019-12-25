package com.cdkhd.npc.enums;

public enum Level {
    //用户的身份等级（eg县代表 or 镇代表）
    TOWN(Byte.valueOf("1")), AREA(Byte.valueOf("2"));

    public final Byte value;

    Level(Byte v) {
        this.value = v;
    }
}
