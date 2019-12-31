package com.cdkhd.npc.enums;

public enum LevelEnum {
    //用户的身份等级（eg县代表 or 镇代表）
    TOWN((byte)1,"镇"),
    AREA((byte)2,"区");

    private Byte value;
    private String name;

    LevelEnum(Byte value, String name) {
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
        LevelEnum[] levelEnums = values();
        for (LevelEnum levelEnum : levelEnums) {
            if (levelEnum.getName().equals(name)) {
                return levelEnum.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        LevelEnum[] levelEnums = values();
        for (LevelEnum levelEnum : levelEnums) {
            if (levelEnum.getValue().equals(value)) {
                return levelEnum.getName();
            }
        }
        return null;
    }
}
