package com.cdkhd.npc.enums;

public enum GovDealStatusEnum {
    //1 未处理 2 已重新分配 3 无需重新分配
    NOT_DEAL((byte)0, "未处理"),
    RE_CONVEY((byte)1, "已重新分配"),
    NOT_NEED_CONVEY((byte)2, "无需重新分配");

    private Byte value;
    private String name;

    GovDealStatusEnum(Byte value, String name){
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
        GovDealStatusEnum[] suggestionStatusEnums = values();
        for (GovDealStatusEnum suggestionStatusEnum : suggestionStatusEnums) {
            if (suggestionStatusEnum.getName().equals(name)) {
                return suggestionStatusEnum.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        GovDealStatusEnum[] suggestionStatusEnums = values();
        for (GovDealStatusEnum suggestionStatusEnum : suggestionStatusEnums) {
            if (suggestionStatusEnum.getValue().equals(value)) {
                return suggestionStatusEnum.getName();
            }
        }
        return null;
    }
}
