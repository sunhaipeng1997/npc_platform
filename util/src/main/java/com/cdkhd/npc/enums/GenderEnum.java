package com.cdkhd.npc.enums;

public enum GenderEnum {
    //用户的身份等级（eg县代表 or 镇代表）
    MALE((byte)1,"男"),
    FEMALE((byte)2,"女");

    private Byte value;
    private String name;

    GenderEnum(Byte value, String name) {
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
        GenderEnum[] levelEnums = values();
        for (GenderEnum levelEnum : levelEnums) {
            if (levelEnum.getName().equals(name)) {
                return levelEnum.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        GenderEnum[] levelEnums = values();
        for (GenderEnum levelEnum : levelEnums) {
            if (levelEnum.getValue().equals(value)) {
                return levelEnum.getName();
            }
        }
        return null;
    }
}
