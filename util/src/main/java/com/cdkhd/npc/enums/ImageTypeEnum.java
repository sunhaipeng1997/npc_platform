package com.cdkhd.npc.enums;

public enum ImageTypeEnum {
    HANDLE_PROCESS((byte)1, "办理流程"),
    HANDLE_RESULT((byte)2, "办理结果");

    private Byte value;
    private String name;

    ImageTypeEnum(Byte val, String name) {
        this.value = val;
        this.name = name;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
