package com.cdkhd.npc.enums;

public enum CommonDictTypeEnum {
    NATION("nation"),
    EDUCATION("education"),
    POLITIC("politic"),
    JOBS("jobs");

    private String value;

    CommonDictTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
