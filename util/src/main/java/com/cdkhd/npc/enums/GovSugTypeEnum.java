package com.cdkhd.npc.enums;

public enum GovSugTypeEnum {
    /**
     * ---------建议状态
     1、待转办建议  2、申请调整单位的建议  3、申请延期的建议  4、办理中的建议  5、已办完的建议  6、已办结的建议
     */
    WAIT_DEAL_SUG((byte)1, "待转办建议"),
    ADJUST_UNIT_SUG((byte)2, "申请调整单位的建议"),
    APPLY_DELAY_SUG((byte)3, "申请延期的建议"),
    DEALING_SUG((byte)4, "办理中的建议"),
    FINISH_SUG((byte)5, "已办完的建议"),
    ACCOMPLISHED_SUG((byte)6, "已办结的建议");

    private Byte value;
    private String name;

    GovSugTypeEnum(Byte value, String name){
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
        GovSugTypeEnum[] suggestionStatusEnums = values();
        for (GovSugTypeEnum suggestionStatusEnum : suggestionStatusEnums) {
            if (suggestionStatusEnum.getName().equals(name)) {
                return suggestionStatusEnum.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        GovSugTypeEnum[] suggestionStatusEnums = values();
        for (GovSugTypeEnum suggestionStatusEnum : suggestionStatusEnums) {
            if (suggestionStatusEnum.getValue().equals(value)) {
                return suggestionStatusEnum.getName();
            }
        }
        return null;
    }
}
