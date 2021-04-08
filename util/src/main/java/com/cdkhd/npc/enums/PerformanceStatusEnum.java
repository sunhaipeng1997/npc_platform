package com.cdkhd.npc.enums;

public enum PerformanceStatusEnum {
    REVOKE((byte)0, "已撤回"),
    NOT_SUBMITTED((byte)1, "未提交"),
    SUBMITTED_AUDIT((byte)2, "待审核"),
    AUDIT_SUCCESS((byte)3, "审核通过"),
    AUDIT_FAILURE((byte)-1, "审核失败");

    private Byte value;
    private String name;

    PerformanceStatusEnum(Byte value, String name) {
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
        PerformanceStatusEnum[] levels = values();
        for (PerformanceStatusEnum level : levels) {
            if (level.getName().equals(name)) {
                return level.getValue();
            }
        }
        return null;
    }

    public static String getName(byte value) {
        PerformanceStatusEnum[] levels = values();
        for (PerformanceStatusEnum level : levels) {
            if (level.getValue() == value) {
                return level.getName();
            }
        }
        return null;
    }
}
