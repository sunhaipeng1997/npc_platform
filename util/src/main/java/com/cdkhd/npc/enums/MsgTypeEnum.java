package com.cdkhd.npc.enums;

public enum  MsgTypeEnum {
    //履职、通知、新闻的审核及其结果
    TO_AUDIT("待审核提醒"),
    AUDIT_RESULT("审核结果通知 "),

    //意见建议的接收和反馈
    NEW_OPINION_OR_SUGGESTION("意见建议提醒"),
    FEEDBACK("意见建议处理提醒"),

    //代表接收的通知
    CONFERENCE("会议通知");

    private String name;

    MsgTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
