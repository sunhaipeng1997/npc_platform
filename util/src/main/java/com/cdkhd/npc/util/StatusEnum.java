package com.cdkhd.npc.util;

/**
 * Created by rfx
 * 2019/10/10 15:25
 */
public enum StatusEnum{
    ENABLED(Constant.LOGIN_WAY_UP, "");

    private Byte code;
    private String name;

    StatusEnum(Byte code, String name) {
        this.code = code;
        this.name = name;
    }

    private byte code() {
        return this.code;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static Byte getCode(String name) {
        StatusEnum[] statusEnums = values();
        for (StatusEnum statusEnum : statusEnums) {
            if (statusEnum.name().equals(name)) {
                return statusEnum.code();
            }
        }
        return null;
    }

    public static String getName(byte code) {
        StatusEnum[] statusEnums = values();
        for (StatusEnum statusEnum : statusEnums) {
            if (statusEnum.code() == code) {
                return statusEnum.getName();
            }
        }
        return null;
    }

}
