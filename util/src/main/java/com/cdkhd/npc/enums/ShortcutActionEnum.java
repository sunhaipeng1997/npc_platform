package com.cdkhd.npc.enums;

public enum ShortcutActionEnum {
    CLOSE("关闭"),

    //选民
    GIVE_ADVICE("提意见"),

    //人大代表
    MAKE_SUGGESTION("提建议"),
    ADD_PERFORMANCE("履职"),

    //新闻管理员扫码预览新闻、人大代表扫码签到
    SCAN_QR_CODE("扫码");

    private String name;

    ShortcutActionEnum(String name) {
        this.name = name;
    }

    public String getKeyword() {
        return this.name();
    }

    public String getName() {
        return name;
    }
}
