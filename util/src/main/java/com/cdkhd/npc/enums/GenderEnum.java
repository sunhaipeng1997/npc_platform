package com.cdkhd.npc.enums;

public enum GenderEnum {
    MALE((byte)1, "男"),
    FEMALE((byte)2, "女");

    private Byte value;
    private String name;

    GenderEnum(Byte v, String n) {
        value = v;
        name = n;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static Byte getValue(String name) {
        for (GenderEnum genderEnum : GenderEnum.values()) {
            if (genderEnum.getName().equals(name)) {
                return genderEnum.value;
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        for (GenderEnum genderEnum : GenderEnum.values()) {
            if (genderEnum.getValue().equals(value)) {
                return genderEnum.getName();
            }
        }
        return "";
    }
}
