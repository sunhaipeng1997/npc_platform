package com.cdkhd.npc.enums;

public enum GroupEnum {
    UNGROUPED("-1","未分组");

    private String value;
    private String name;

    GroupEnum(String value, String name) {
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
        GroupEnum[] levels = values();
        for (GroupEnum level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(String value) {
        GroupEnum[] levels = values();
        for (GroupEnum level : levels) {
            if (level.getValue().equals(value)) {
                return level.getName();
            }
        }
        return null;
    }
}
