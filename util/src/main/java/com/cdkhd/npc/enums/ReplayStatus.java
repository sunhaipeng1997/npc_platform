package com.cdkhd.npc.enums;

public enum ReplayStatus {
    UNANSWERED((byte)1,"未回复"),
    ANSWERED((byte)2,"已回复");

    private Byte value;
    private String name;

    ReplayStatus(Byte value, String name) {
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
        ReplayStatus[] levels = values();
        for (ReplayStatus level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(byte value) {
        ReplayStatus[] levels = values();
        for (ReplayStatus level : levels) {
            if (level.getValue() == value) {
                return level.getName();
            }
        }
        return null;
    }
}
