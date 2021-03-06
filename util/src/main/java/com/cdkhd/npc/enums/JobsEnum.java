package com.cdkhd.npc.enums;

public enum JobsEnum {
    NORMAL("MEMBER","代表"),
    LEADER("CHAIRMAN","人大领导"),
    SPECIAL("SPECIAL_MAN","办公室人员");

    private String value;
    private String name;

    JobsEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }


    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static String getValue(String name) {
        JobsEnum[] levels = values();
        for (JobsEnum level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(String value) {
        JobsEnum[] levels = values();
        for (JobsEnum level : levels) {
            if (level.getValue().equals(value)) {
                return level.getName();
            }
        }
        return null;
    }
}
