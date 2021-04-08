package com.cdkhd.npc.enums;

public enum ViewEnum {
    CHECKED((byte)1,"已查看"),
    UNREAD((byte)0,"未查看");

    private Byte value;
    private String name;

    ViewEnum(Byte value, String name) {
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
        ViewEnum[] levels = values();
        for (ViewEnum level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(byte value) {
        ViewEnum[] levels = values();
        for (ViewEnum level : levels) {
            if (level.getValue() == value) {
                return level.getName();
            }
        }
        return null;
    }
}
