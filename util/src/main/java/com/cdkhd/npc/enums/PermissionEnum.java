package com.cdkhd.npc.enums;

public enum PermissionEnum {
    //小程序
    //代表之家
    MEMBER_INFO("代表风采"),
    VIEW_WORKSTATION("查看联络点"),
    MY_OPINION("我的意见"),
    MY_SUGGESTION("我的建议"),
    MY_PERFORMANCE("我的履职"),
    RECEIVE_OPINION("收到意见"),

    RECEIVE_NOTICE("接收通知"),
//    ANNOUNCEMENT("接收公告"),

    MEMBER_RANK("代表排名"),
    TOWN_RANK("各镇履职排名"),
    STREET_RANK("街道履职排名"),

    AUDIT_NEWS("审核新闻"),
    AUDIT_SUGGESTION("审核建议"),
    AUDIT_PERFORMANCE("审核履职"),
    AUDIT_NOTICE("审核通知"),

    //建议办理
    DEAL_MY_SUGGESTION("我的建议"),
    DEAL_OTHERS_SUGGESTION("他人建议"),
    DEAL_SECONDED_SUGGESTION("附议建议"),
    DEAL_AUDIT_SUGGESTION("审核建议"),
    DEAL_CONVEY_SUGGESTIONS("转办建议"),
    GOV_DELAY_SUGGESTION("延期建议"),
    GOV_ADJUST_SUGGESTION("调整单位"),
    GOV_URGE_SUGGESTION("查看建议情况"),
    UNIT_WAIT_DEAL_SUGGESTION("单位待办理"),
    UNIT_DEAL_SUGGESTION("单位办理建议"),

    //后台
    HOMEPAGE("首页"),
    ACCOUNT_MANAGE("账号管理"),
    NEWS_TYPE_MANAGE("新闻类型管理"),
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
    TOWN_SUGGESTION_MANAGE("各镇代表建议管理"),
    PERFORMANCE_TYPE("代表履职类型"),
    PERFORMANCE_MANAGE("代表履职管理"),
    TOWN_PERFORMANCE_MANAGE("各镇代表履职管理"),
    SUGGESTION_COUNT("代表建议统计"),
    PERFORMANCE_COUNT("代表履职统计"),
    TOWN_PERFORMANCE_COUNT("各镇履职统计"),
    PERMISSION_MANAGE("代表权限管理"),
    SESSION_MANAGE("届期管理"),
    SYSTEM_SETTING("系统设置"),
    TOWN_MANAGE("镇管理"),
    VILLAGE_MANAGE("村管理"),
    //建议办理
    NPC_HOMEPAGE_DEAL("人大管理员建议办理首页"),
    GOVERNMENT_MANAGE("政府管理"),
    SUGGESTION_TYPE_DEAL("建议类型管理"),
    SUGGESTION_DEAL("代表建议管理"),


    //政府
    GOV_HOMEPAGE_DEAL("政府建议办理首页"),
    GOV_WAIT_CONVEY("待转办"),
    GOV_ADJUST_CONVEY("调整单位"),
    GOV_ADJUST_DELAY("申请延期"),
    GOV_DEALING("办理中列表"),
    GOV_FINISHED("办理完成列表"),
    GOV_COMPLETED("办结列表"),
    UNIT_MANAGE("办理单位管理"),
    SUGGESTION_SETTING("建议设置"),
//    GOV_COUNT("统计"),

    //办理单位
    UNIT_HOMEPAGE_DEAL("办理单位建议办理首页"),
    UNIT_WAIT_DEAL("办理单待办理列表"),
    UNIT_DEALING("办理单位办理中列表"),
    UNIT_DEAL_DONE("办理单位办理完成列表"),
    UNIT_DEAL_COMPLETED("办理单位办结列表");


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
