package com.cdkhd.npc.enums;

public enum MenuEnum {
    //小程序菜单类型
    COMMUNICATION(null, "互动交流",(byte)1, null, null,null,"MEMBER_HOUSE"),//name   type   url   parent
    NOTIFICATION(null, "通知公告",(byte)1, null, null,null, "MEMBER_HOUSE"),
    STATISTICAL_RANKING(null, "排名统计",(byte)1, null, null,null, "MEMBER_HOUSE"),
    SPECIAL_FUNCTIONS(null, "特殊职能",(byte)1, null, null,null, "MEMBER_HOUSE"),
    //小程序菜单
    //互动交流
    MEMBER_INFO(null, "代表风采",(byte)1,"personGrace","representative-grace/representative-group","COMMUNICATION", "MEMBER_HOUSE"),
    MY_OPINION(null, "我的意见",(byte)1, "suggestionMine", "opinion/opinion-voter","COMMUNICATION", "MEMBER_HOUSE"),
    RECEIVE_OPINION(null, "收到意见",(byte)1, "suggestionOthers", "opinion/opinion-representative","COMMUNICATION", "MEMBER_HOUSE"),
    MY_SUGGESTION(null, "我的建议",(byte)1, "auditNotif", "suggestion/my-suggestion","COMMUNICATION", "MEMBER_HOUSE"),
    MY_PERFORMANCE(null, "我的履职",(byte)1, "dutySubmit", "performance/my-performance","COMMUNICATION", "MEMBER_HOUSE"),
    WORK_STATION(null, "工作站",(byte)1, "workstation", "workstation/workstation","COMMUNICATION", "MEMBER_HOUSE"),
//    SECONDED_SUGGESTION("我的附议",(byte)1,null,"COMMUNICATION"),

    //通知公告
    NOTIFICATION_INFO(null, "通知信息",(byte)1, "notifition", "notification/notify-list","NOTIFICATION", "MEMBER_HOUSE"),
//    ANNOUNCEMENT(null, "公告信息",(byte)1, "announcement", "notification/announceList","NOTIFICATION", "MEMBER_HOUSE"),

    //统计排名
    MEMBER_RANK(null, "代表排名",(byte)1, "opinionVoter", "statistics/statistics-representative","STATISTICAL_RANKING", "MEMBER_HOUSE"),
    TOWN_RANK(null, "各镇履职排名",(byte)1, "statistics-town", "statistics/statistics-town","STATISTICAL_RANKING", "MEMBER_HOUSE"),
    STREET_RANK(null, "街道履职排名",(byte)1, "statistics-town", "statistics/statistics-street","STATISTICAL_RANKING", "MEMBER_HOUSE"),

    //特殊职能
    AUDIT_NEWS(null, "新闻审核",(byte)1, "auditNews", "news/news-review-list","SPECIAL_FUNCTIONS", "MEMBER_HOUSE"),
    AUDIT_SUGGESTION(null, "建议审核",(byte)1, "auditSuggestion", "suggestion/audit-pending-suggestion","SPECIAL_FUNCTIONS", "MEMBER_HOUSE"),
    AUDIT_PERFORMANCE(null, "履职审核",(byte)1, "auditDuty", "performance/audit-performance","SPECIAL_FUNCTIONS", "MEMBER_HOUSE"),
    AUDIT_NOTICE(null, "通知审核",(byte)1, "audit-notifition", "notification/notify-review-list","SPECIAL_FUNCTIONS", "MEMBER_HOUSE"),

    //后台菜单
    //类型管理
    HOMEPAGE("index", "首页",(byte)2, "menu-dashboard", "/member_house/index",null, "MEMBER_HOUSE"),
    ACCOUNT_MANAGE("account", "账号管理",(byte)2, "menu-account", "/member_house/account",null, "MEMBER_HOUSE"),
    NEWS_MANAGE("news", "新闻管理",(byte)2, "menu-news", "/member_house/news",null, "MEMBER_HOUSE"),
    NOTICE_MANAGE("notification", "通知管理",(byte)2, "menu-notify", "/member_house/notification",null, "MEMBER_HOUSE"),
    NPC_MANAGE("members", "代表管理",(byte)2, "menu-performance", "/member_house/members",null, "MEMBER_HOUSE"),
    OPINION_MANAGE("opinions", "选民意见管理",(byte)2, "menu-opinion", "/member_house/opinions",null, "MEMBER_HOUSE"),
    SUGGESTION_MANAGE("suggestion", "代表建议管理",(byte)2, "menu-suggestion", "/member_house/suggestion",null, "MEMBER_HOUSE"),
    TOWN_SUGGESTION_MANAGE("townSuggestion", "各镇代表建议管理",(byte)2, "menu-suggestion", "/member_house/townSuggestion",null, "MEMBER_HOUSE"),
    PERFORMANCE_MANAGE("performance", "代表履职管理",(byte)2, "menu-performanceDuty", "/member_house/performance",null, "MEMBER_HOUSE"),
    TOWN_PERFORMANCE_MANAGE("townPerformance", "各镇代表履职管理",(byte)2, "menu-performanceDuty", "/member_house/townPerformance",null, "MEMBER_HOUSE"),
    STUDY_MANAGE("studyFile", "学习资料管理",(byte)2, "menu-studyFile", "/member_house/studyFile",null, "MEMBER_HOUSE"),
    PERMISSION_MANAGE("special", "代表权限管理",(byte)2, "menu-special", "/member_house/special",null, "MEMBER_HOUSE"),
    SYSTEM_SETTING("systemSetting", "系统设置",(byte)2, "menu-setting", "/member_house/systemSetting",null, "MEMBER_HOUSE"),

    TYPE_MANAGE("typeManage", "类型管理",(byte)2, null, null,null, "MEMBER_HOUSE"),
    STUDY_TYPE_MANAGE("studyCate", "学习类型管理",(byte)2, "menu-studyType", "/member_house/studyCate","TYPE_MANAGE", "MEMBER_HOUSE"),
    NEWS_TYPE_MANAGE("newsType", "新闻类型管理",(byte)2, "menu-studyType", "/member_house/newsType","TYPE_MANAGE", "MEMBER_HOUSE"),
    SUGGESTION_TYPE_MANAGE("suggestionBusiness", "建议类型管理",(byte)2, "menu-suggestionType", "/member_house/suggestionBusiness","TYPE_MANAGE", "MEMBER_HOUSE"),
    PERFORMANCE_TYPE_MANAGE("performanceType", "履职类型管理",(byte)2, "menu-performanceType", "/member_house/performanceType","TYPE_MANAGE", "MEMBER_HOUSE"),

    BASIC_MANAGE("basicInfo", "基本信息管理",(byte)2, null, null,null, "MEMBER_HOUSE"),
    NPC_MEMBER_GROUP("groups", "代表小组管理",(byte)2, "menu-group", "/member_house/groups","BASIC_MANAGE", "MEMBER_HOUSE"),
    WORKSTATION_MANAGE("workStation", "工作站管理",(byte)2, "menu-workstation", "/member_house/workStation","BASIC_MANAGE", "MEMBER_HOUSE"),
    SESSION_MANAGE("sessions", "届期管理",(byte)2, "menu-worktime", "/member_house/sessions","BASIC_MANAGE", "MEMBER_HOUSE"),
    TOWN_MANAGE("towns", "镇/街道管理",(byte)2, "menu-town", "/member_house/towns","BASIC_MANAGE", "MEMBER_HOUSE"),
    VILLAGE_MANAGE("villages", "村管理",(byte)2, "menu-village", "/member_house/villages","BASIC_MANAGE", "MEMBER_HOUSE"),

    STATISTICS_MANAGE("statisticsManage", "统计",(byte)2, null, null,null, "MEMBER_HOUSE"),
    SUGGESTION_COUNT("suggestionCount", "代表建议统计",(byte)2, "menu-statistics", "/member_house/suggestion_count","STATISTICS_MANAGE", "MEMBER_HOUSE"),
    PERFORMANCE_COUNT("performanceCount", "代表履职统计",(byte)2, "menu-statisics2", "/member_house/performanceCount","STATISTICS_MANAGE", "MEMBER_HOUSE"),
    TOWN_PERFORMANCE_COUNT("townPerformanceCount", "各镇履职统计",(byte)2, "menu-statisics2", "/member_house/townPerformanceCount","STATISTICS_MANAGE", "MEMBER_HOUSE");


    private String route;
    private String name;
    private Byte type;
    private String icon;
    private String url;
    private String parentId;
    private String system;

    MenuEnum(String route, String name, Byte type, String icon, String url, String parentId, String system) {
        this.route = route;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.url = url;
        this.parentId = parentId;
        this.system = system;
    }

    public String getRoute() {
        return route;
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
