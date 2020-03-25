package com.cdkhd.npc.enums;

public enum MenuEnum {
    //小程序菜单类型
    COMMUNICATION("互动交流",(byte)1, null, null,null,"MEMBER_HOUSE"),//name   type   url   parent
    NOTIFICATION("通知公告",(byte)1, null, null,null, "MEMBER_HOUSE"),
    STATISTICAL_RANKING("排名统计",(byte)1, null, null,null, "MEMBER_HOUSE"),
    SPECIAL_FUNCTIONS("特殊职能",(byte)1, null, null,null, "MEMBER_HOUSE"),
    //小程序菜单
    //互动交流
    MEMBER_INFO("代表风采",(byte)1,"personGrace","representative-grace/representative-group","COMMUNICATION", "MEMBER_HOUSE"),
    WORK_STATION("工作站",(byte)1, "workstation", "workstation/workstation","COMMUNICATION", "MEMBER_HOUSE"),
    MY_OPINION("我的意见",(byte)1, "suggestionMine", "opinion/opinion-voter","COMMUNICATION", "MEMBER_HOUSE"),
    MY_SUGGESTION("我的建议",(byte)1, "auditNotif", "suggestion/my-suggestion","COMMUNICATION", "MEMBER_HOUSE"),
    RECEIVE_OPINION("收到意见",(byte)1, "suggestionOthers", "opinion/opinion-representative","COMMUNICATION", "MEMBER_HOUSE"),
    MY_PERFORMANCE("我的履职",(byte)1, "dutySubmit", "performance/my-performance","COMMUNICATION", "MEMBER_HOUSE"),
//    SECONDED_SUGGESTION("我的附议",(byte)1,null,"COMMUNICATION"),

    //通知公告
    NOTIFICATION_INFO("通知信息",(byte)1, "notifition", "notification/notify-list","NOTIFICATION", "MEMBER_HOUSE"),
    ANNOUNCEMENT("公告信息",(byte)1, "announcement", "notification/announceList","NOTIFICATION", "MEMBER_HOUSE"),

    //统计排名
    MEMBER_RANK("代表排名",(byte)1, "opinionVoter", "statistics/statistics-representative","STATISTICAL_RANKING", "MEMBER_HOUSE"),
    TOWN_RANK("各镇排名",(byte)1, "statistics-town", "statistics/statistics-town","STATISTICAL_RANKING", "MEMBER_HOUSE"),

    //特殊职能
    AUDIT_NEWS("新闻审核",(byte)1, "auditNews", "news/news-review-list","SPECIAL_FUNCTIONS", "MEMBER_HOUSE"),
    AUDIT_SUGGESTION("审核建议",(byte)1, "auditSuggestion", "suggestion/my-suggestion","SPECIAL_FUNCTIONS", "MEMBER_HOUSE"),
    AUDIT_PERFORMANCE("履职审核",(byte)1, "auditDuty", "performance/audit-performance","SPECIAL_FUNCTIONS", "MEMBER_HOUSE"),
    AUDIT_NOTICE("通知审核",(byte)1, "audit-notifition", "notification/notify-review-list","SPECIAL_FUNCTIONS", "MEMBER_HOUSE"),

    //后台菜单
    //类型管理
    TYPE_MANAGE("类型管理",(byte)2, null, null,null, "MEMBER_HOUSE"),
    BASIC_MANAGE("基本信息管理",(byte)2, null, null,null, "MEMBER_HOUSE"),

    ACCOUNT_MANAGE("账号管理",(byte)2, "menu-account", "/member_house/account",null, "MEMBER_HOUSE"),
    NEWS_MANAGE("新闻管理",(byte)2, "menu-news", "/member_house/news",null, "MEMBER_HOUSE"),
    NOTICE_MANAGE("通知管理",(byte)2, "menu-notify", "/member_house/notification",null, "MEMBER_HOUSE"),
    NPC_MANAGE("代表管理",(byte)2, "menu-performance", "/member_house/members",null, "MEMBER_HOUSE"),
    NPC_MEMBER_GROUP("代表小组管理",(byte)2, "menu-group", "/member_house/groups",null, "MEMBER_HOUSE"),
    WORKSTATION_MANAGE("工作站管理",(byte)2, "menu-workstation", "/member_house/workStation",null, "MEMBER_HOUSE"),
    OPINION_MANAGE("选民意见管理",(byte)2, "menu-opinion", "/member_house/opinions",null, "MEMBER_HOUSE"),
    STUDY_TYPE_MANAGE("学习类型管理",(byte)2, "menu-studyType", "/member_house/studyCate",null, "MEMBER_HOUSE"),
    STUDY_MANAGE("学习资料管理",(byte)2, "menu-studyFile", "/member_house/studyFile",null, "MEMBER_HOUSE"),
    SUGGESTION_TYPE_MANAGE("建议类型管理",(byte)2, "menu-suggestionType", "/member_house/suggestionBusiness",null, "MEMBER_HOUSE"),
    SUGGESTION_MANAGE("代表建议管理",(byte)2, "menu-suggestion", "/member_house/suggestion",null, "MEMBER_HOUSE"),
    PERFORMANCE_TYPE_MANAGE("履职类型管理",(byte)2, "menu-performanceType", "/member_house/performanceType",null, "MEMBER_HOUSE"),
    PERFORMANCE_MANAGE("代表履职管理",(byte)2, "menu-performanceDuty", "/member_house/performance",null, "MEMBER_HOUSE"),
    SUGGESTION_COUNT("代表建议统计",(byte)2, "menu-statistics", "/member_house/suggestion_count",null, "MEMBER_HOUSE"),
    PERFORMANCE_COUNT("代表履职统计",(byte)2, "menu-statisics2", "/member_house/performance_count",null, "MEMBER_HOUSE"),
    PERMISSION_MANAGE("代表权限管理",(byte)2, "menu-special", "/member_house/special",null, "MEMBER_HOUSE"),
    SESSION_MANAGE("届期管理",(byte)2, "menu-worktime", "/member_house/sessions",null, "MEMBER_HOUSE"),
    TOWN_MANAGE("镇管理",(byte)2, "menu-town", "/member_house/towns",null, "MEMBER_HOUSE"),
    VILLAGE_MANAGE("村管理",(byte)2, "menu-village", "/member_house/villages",null, "MEMBER_HOUSE"),
    SYSTEM_SETTING("系统设置",(byte)2, "menu-setting", "/member_house/systemSetting",null, "MEMBER_HOUSE");

    private String name;
    private Byte type;
    private String icon;
    private String url;
    private String parentId;
    private String system;

    MenuEnum(String name, Byte type, String icon, String url, String parentId, String system) {
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.url = url;
        this.parentId = parentId;
        this.system = system;
    }

    public String getName() {
        return this.name;
    }

    public Byte getType() {
        return type;
    }

    public String getIcon() {
        return icon;
    }

    public String getUrl() {
        return url;
    }

    public String getParentId() {
        return parentId;
    }

    public String getSystem() {
        return system;
    }
}
