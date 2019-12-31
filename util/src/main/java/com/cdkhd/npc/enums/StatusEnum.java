package com.cdkhd.npc.enums;

public enum StatusEnum {
    ENABLED((byte)1,"启用"),
    DISABLED((byte)2,"停用");

    private Byte value;
    private String name;

    StatusEnum(Byte value, String name) {
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
        StatusEnum[] levels = values();
        for (StatusEnum level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(byte value) {
        StatusEnum[] levels = values();
        for (StatusEnum level : levels) {
            if (level.getValue() == value) {
                return level.getName();
            }
        }
        return null;
    }
}
