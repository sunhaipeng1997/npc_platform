package com.cdkhd.npc.enums;

public enum TownTypeEnum {
    //用户的身份等级（eg县代表 or 镇代表）
    TOWN((byte)1,"镇"),
    STREET((byte)2,"街道");

    private Byte value;
    private String name;

    TownTypeEnum(Byte value, String name) {
        this.value = value;
        this.name = name;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static Byte getValue(String name) {
        TownTypeEnum[] levelEnums = values();
        for (TownTypeEnum levelEnum : levelEnums) {
            if (levelEnum.getName().equals(name)) {
                return levelEnum.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        TownTypeEnum[] levelEnums = values();
        for (TownTypeEnum levelEnum : levelEnums) {
            if (levelEnum.getValue().equals(value)) {
                return levelEnum.getName();
            }
        }
        return null;
    }
}
