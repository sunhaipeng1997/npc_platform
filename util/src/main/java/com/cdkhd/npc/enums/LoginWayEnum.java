package com.cdkhd.npc.enums;

public enum LoginWayEnum {
    LOGIN_UP((byte)1, "后台管理员账号密码登录"),
    LOGIN_WECHAT((byte)2, "小程序微信登录");

    private Byte value;
    private String name;

    LoginWayEnum(Byte value, String name) {
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
        LoginWayEnum[] levelEnums = values();
        for (LoginWayEnum LoginWayEnum : levelEnums) {
            if (LoginWayEnum.getName().equals(name)) {
                return LoginWayEnum.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        LoginWayEnum[] levelEnums = values();
        for (LoginWayEnum LoginWayEnum : levelEnums) {
            if (LoginWayEnum.getValue().equals(value)) {
                return LoginWayEnum.getName();
            }
        }
        return null;
    }
}
