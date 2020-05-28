package com.cdkhd.npc.enums;

public enum ConveyStatusEnum {

    CONVEYING((byte)0, "单位待处理"),
    CONVEY_SUCCESS((byte)1, "单位接受"),
    CONVEY_FAILED((byte)2, "单位拒绝");

    private Byte value;
    private String name;

    ConveyStatusEnum(Byte value, String name){
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
        ConveyStatusEnum[] suggestionStatusEnums = values();
        for (ConveyStatusEnum suggestionStatusEnum : suggestionStatusEnums) {
            if (suggestionStatusEnum.getName().equals(name)) {
                return suggestionStatusEnum.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        ConveyStatusEnum[] suggestionStatusEnums = values();
        for (ConveyStatusEnum suggestionStatusEnum : suggestionStatusEnums) {
            if (suggestionStatusEnum.getValue().equals(value)) {
                return suggestionStatusEnum.getName();
            }
        }
        return null;
    }
}
