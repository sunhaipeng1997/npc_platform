package com.cdkhd.npc.enums;

public enum MenuEnum {
    //小程序菜单
    MY_OPINION("我的意见"),
    RECEIVE_OPINION("收到意见"),
    EDIT_SUGGESTION("提出建议"),
    MY_SUGGESTION("我的建议"),
    AUDIT_SUGGESTION("审核建议"),
    SECONDED_SUGGESTION("我的附议"),
    EDIT_PERFORMANCE("添加履职"),
    MY_PERFORMANCE("我的履职"),
    AUDIT_PERFORMANCE("审核履职"),
    AUDIT_NEWS("审核新闻"),
    AUDIT_NOTICE("审核通知"),

    //后台菜单
    ACCOUNT_MANAGE("账号管理"),
    STUDY_MANAGE("学习资料管理"),
    WORKSTATION_MANAGE("联络点管理"),
    NEWS_MANAGE("新闻管理"),
    NOTICE_MANAGE("通知管理"),
    NPC_MANAGE("代表管理"),
    TOWN_MANAGE("镇管理"),
    GROUP_MANAGE("小组管理"),
    VILLAGE_MANAGE("村管理"),
    OPINION_MANAGE("选民意见管理"),
    SUGGESTION_MANAGE("代表建议管理"),
    PERFORMANCE_MANAGE("代表履职管理"),
    STATISTICS("统计分析"),
    PERMISSION_MANAGE("代表权限管理"),
    SESSION_MANAGE("届期管理");

    private String name;

    MenuEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
