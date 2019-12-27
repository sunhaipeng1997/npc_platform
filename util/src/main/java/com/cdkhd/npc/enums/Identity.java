package com.cdkhd.npc.enums;

public enum Identity {
    //代表的身份
    TOWN((byte)1,"镇"),
    AREA((byte)2,"区");

    private Byte value;
    private String name;

    Identity(Byte value, String name) {
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
        Identity[] levels = values();
        for (Identity level : levels) {
            if (level.name().equals(name)) {
                return level.value();
            }
        }
        return null;
    }

    public static String getName(byte value) {
        Identity[] levels = values();
        for (Identity level : levels) {
            if (level.value() == value) {
                return level.getName();
            }
        }
        return null;
    }
}
