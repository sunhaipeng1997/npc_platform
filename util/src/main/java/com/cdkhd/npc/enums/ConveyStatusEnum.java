package com.cdkhd.npc.enums;

public enum ConveyStatusEnum {

    CONVEYING((byte)0, "转办中"),
    CONVEY_SUCCESS((byte)1, "转办成功"),
    CONVEY_FAILED((byte)2, "转办失败");

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
