package com.cdkhd.npc.enums;

public enum JobsEnum {
    NORMAL((byte)1,"普通代表"),
    LEADER((byte)2,"人大主席"),
    SPECIAL((byte)3,"特殊人员");

    private Byte value;
    private String name;

    JobsEnum(Byte value, String name) {
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
        JobsEnum[] levels = values();
        for (JobsEnum level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(byte value) {
        JobsEnum[] levels = values();
        for (JobsEnum level : levels) {
            if (level.getValue() == value) {
                return level.getName();
            }
        }
        return null;
    }
}
