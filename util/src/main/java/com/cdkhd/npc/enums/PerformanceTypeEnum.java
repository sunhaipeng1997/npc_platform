package com.cdkhd.npc.enums;

public enum PerformanceTypeEnum {
    SUGGESTION("提建议"),
    OTHER("其他");

    private String value;

    PerformanceTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
