package com.cdkhd.npc.enums;

public enum Level {
    //用户的身份等级（eg县代表 or 镇代表）
    TOWN((byte)1,"镇"),
    AREA((byte)2,"区");

    private Byte value;
    private String name;

    Level(Byte value, String name) {
        this.value = value;
        this.name = name;
    }

    private byte value() {
        return this.value;
    }

    public byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static Byte getValue(String name) {
        Level[] levels = values();
        for (Level level : levels) {
            if (level.name().equals(name)) {
                return level.value();
            }
        }
        return null;
    }

    public static String getName(byte value) {
        Level[] levels = values();
        for (Level level : levels) {
            if (level.value() == value) {
                return level.getName();
            }
        }
        return null;
    }
}
