package com.cdkhd.npc.enums;

public enum UnitTypeEnum {
    MAIN_UNIT((byte)1,"主办单位"),
    CO_UNIT((byte)2,"协办单位");

    private Byte value;
    private String name;

    UnitTypeEnum(Byte value, String name) {
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
        UnitTypeEnum[] levelEnums = values();
        for (UnitTypeEnum levelEnum : levelEnums) {
            if (levelEnum.getName().equals(name)) {
                return levelEnum.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        UnitTypeEnum[] levelEnums = values();
        for (UnitTypeEnum levelEnum : levelEnums) {
            if (levelEnum.getValue().equals(value)) {
                return levelEnum.getName();
            }
        }
        return null;
    }
}
