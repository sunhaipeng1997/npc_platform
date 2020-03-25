package com.cdkhd.npc.enums;

public enum PermissionEnum {
    //小程序
    MEMBER_INFO("代表风采"),
    VIEW_WORKSTATION("查看联络点"),
    MY_OPINION("我的意见"),
    MY_SUGGESTION("我的建议"),
    MY_PERFORMANCE("我的履职"),
    RECEIVE_OPINION("收到意见"),
//    OTHERS_SUGGESTION("他人建议"),
//    SECONDED_SUGGESTION("附议建议"),

    RECEIVE_NOTICE("接收通知"),
    ANNOUNCEMENT("接收公告"),

    MEMBER_RANK("代表排名"),
    TOWN_RANK("各镇排名"),

    AUDIT_NEWS("审核新闻"),
    AUDIT_SUGGESTION("审核建议"),
    AUDIT_PERFORMANCE("审核履职"),
    AUDIT_NOTICE("审核通知"),

    //后台
    ACCOUNT_MANAGE("账号管理"),
    NEWS_MANAGE("新闻管理"),
    NOTICE_MANAGE("通知管理"),
    NPC_MANAGE("代表管理"),
    GROUP_MANAGE("小组管理"),
    WORKSTATION_MANAGE("工作站管理"),
    OPINION_MANAGE("选民意见管理"),
    STUDY_TYPE_MANAGE("学习类型管理"),
    STUDY_MANAGE("学习资料管理"),
    SUGGESTION_TYPE("代表建议类型"),
    SUGGESTION_MANAGE("代表建议管理"),
    PERFORMANCE_TYPE("代表履职类型"),
    PERFORMANCE_MANAGE("代表履职管理"),
    SUGGESTION_COUNT("代表建议统计"),
    PERFORMANCE_COUNT("代表履职统计"),
    PERMISSION_MANAGE("代表权限管理"),
    SESSION_MANAGE("届期管理"),
    SYSTEM_SETTING("系统设置"),
    TOWN_MANAGE("镇管理"),
    VILLAGE_MANAGE("村管理");

    private String name;

    PermissionEnum(String name) {
        this.name = name;
    }

    public String getKeyword() {
        return this.name();
    }

    public String getName() {
        return name;
    }
}
