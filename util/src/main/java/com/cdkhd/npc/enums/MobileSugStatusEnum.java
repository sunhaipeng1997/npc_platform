package com.cdkhd.npc.enums;

public enum MobileSugStatusEnum {
    //小程序查看建议，分状态（全部、已审核、未审核）
    All((byte)1,"全部"),
    TO_BE_AUDITED((byte)2,"未审核"),
    HAS_BEEN_AUDITED((byte)3,"已审核");

    private Byte value;
    private String name;

    MobileSugStatusEnum(Byte value, String name) {
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
        MobileSugStatusEnum[] levels = values();
        for (MobileSugStatusEnum level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(byte value) {
        MobileSugStatusEnum[] levels = values();
        for (MobileSugStatusEnum level : levels) {
            if (level.getValue() == value) {
                return level.getName();
            }
        }
        return null;
    }
}
