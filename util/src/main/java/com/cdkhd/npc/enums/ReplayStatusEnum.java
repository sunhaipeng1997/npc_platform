package com.cdkhd.npc.enums;

public enum ReplayStatusEnum {
    UNANSWERED((byte)1,"未回复"),
    ANSWERED((byte)2,"已回复");

    private Byte value;
    private String name;

    ReplayStatusEnum(Byte value, String name) {
        this.value = value;
        this.name = name;
    }


    public byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static Byte getValue(String name) {
        ReplayStatusEnum[] levels = values();
        for (ReplayStatusEnum level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(byte value) {
        ReplayStatusEnum[] levels = values();
        for (ReplayStatusEnum level : levels) {
            if (level.getValue() == value) {
                return level.getName();
            }
        }
        return null;
    }
}
