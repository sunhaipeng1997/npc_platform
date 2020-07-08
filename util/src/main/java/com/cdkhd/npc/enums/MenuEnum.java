package com.cdkhd.npc.enums;

public enum MenuEnum {
    //小程序菜单类型
    //代表之家
    COMMUNICATION(null, "互动交流",(byte)1, null, null,null,"MEMBER_HOUSE"),//name   type   url   parent
    NOTIFICATION(null, "通知公告",(byte)1, null, null,null, "MEMBER_HOUSE"),
    STATISTICAL_RANKING(null, "排名统计",(byte)1, null, null,null, "MEMBER_HOUSE"),
    SPECIAL_FUNCTIONS(null, "特殊职能",(byte)1, null, null,null, "MEMBER_HOUSE"),
    //建议办理
    MY_SUGGESTION_FOR_DEAL(null, "我的建议",(byte)1, null, null,null,"SUGGESTION"),
    SECONDED_SUGGESTION_FOR_DEAL(null, "建议附议",(byte)1, null, null,null,"SUGGESTION"),
    SUGGESTION_AUDIT_FOR_DEAL(null, "建议审核",(byte)1, null, null,null,"SUGGESTION"),
    SUGGESTION_CONVEY_FOR_DEAL(null, "建议转办",(byte)1, null, null,null,"SUGGESTION"),
    SUGGESTION_DEAL_FOR_DEAL(null, "建议办理",(byte)1, null, null,null,"SUGGESTION"),

    //小程序菜单
    //代表之家
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

    //建议办理
    //我的建议
    SUGGESTION_DRAFT(null, "草稿",(byte)1,"SUGGESTION_DRAFT","npc/my_draft","MY_SUGGESTION_FOR_DEAL", "SUGGESTION"),
    SUGGESTION_COMMITTED(null, "已提交",(byte)1,"SUGGESTION_COMMITTED","npc/my_submit","MY_SUGGESTION_FOR_DEAL", "SUGGESTION"),
    SUGGESTION_DONE(null, "已办完",(byte)1,"SUGGESTION_DONE","npc/my_done","MY_SUGGESTION_FOR_DEAL", "SUGGESTION"),
    SUGGESTION_COMPLETED(null, "已办结",(byte)1,"SUGGESTION_COMPLETED","npc/my_complete","MY_SUGGESTION_FOR_DEAL", "SUGGESTION"),

    //附议建议
    OTHERS_SUGGESTIONS(null, "我能附议的",(byte)1,"OTHERS_SUGGESTIONS","npc/can_second","SECONDED_SUGGESTION_FOR_DEAL", "SUGGESTION"),
    SECONDED_SUGGESTIONS(null, "我附议的",(byte)1,"SECONDED_SUGGESTIONS","npc/seconded","SECONDED_SUGGESTION_FOR_DEAL", "SUGGESTION"),
    SECONDED_SUGGESTIONS_COMPLETED(null, "附议办结的",(byte)1,"SECONDED_SUGGESTIONS_COMPLETED","npc/second_done","SECONDED_SUGGESTION_FOR_DEAL", "SUGGESTION"),

    //建议审核
    WAIT_AUDIT_SUGGESTIONS(null, "待审核建议",(byte)1,"WAIT_AUDIT_SUGGESTIONS","auditor/to_audit","SUGGESTION_AUDIT_FOR_DEAL", "SUGGESTION"),
    AUDIT_PASS_SUGGESTIONS(null, "审核通过的建议",(byte)1,"AUDIT_PASS_SUGGESTIONS","auditor/audit_pass","SUGGESTION_AUDIT_FOR_DEAL", "SUGGESTION"),
    AUDIT_FAILED_SUGGESTIONS(null, "审核失败的建议",(byte)1,"AUDIT_FAILED_SUGGESTIONS","auditor/audit_refuse","SUGGESTION_AUDIT_FOR_DEAL", "SUGGESTION"),

    // 建议转办
    WAIT_CONVEY_SUGGESTIONS(null, "待转办",(byte)1,"WAIT_CONVEY_SUGGESTIONS","gov/to_forward","SUGGESTION_CONVEY_FOR_DEAL", "SUGGESTION"),
//    CONVEYED_SUGGESTIONS(null, "已转办",(byte)1,"CONVEYED_SUGGESTIONS","gov/dealing","SUGGESTION_CONVEY_FOR_DEAL", "SUGGESTION"),
    APPLY_DELAY_SUGGESTIONS(null, "延期申请",(byte)1,"APPLY_DELAY_SUGGESTIONS","gov/delay_apply","SUGGESTION_CONVEY_FOR_DEAL", "SUGGESTION"),
    APPLY_ADJUST_SUGGESTIONS(null, "调整单位申请",(byte)1,"APPLY_ADJUST_SUGGESTIONS","gov/adjust_apply","SUGGESTION_CONVEY_FOR_DEAL", "SUGGESTION"),
    GOV_DEALING_SUGGESTIONS(null, "办理中",(byte)1,"DEALING_SUGGESTIONS","gov/dealing","SUGGESTION_DEAL_FOR_DEAL", "SUGGESTION"),
    GOV_FINISHED_SUGGESTIONS(null, "已办完",(byte)1,"DEAL_DONE_SUGGESTIONS","gov/finished","SUGGESTION_DEAL_FOR_DEAL", "SUGGESTION"),
    GOV_COMPLETED_SUGGESTIONS(null, "已办结",(byte)1,"DEAL_COMPLETED_SUGGESTIONS","gov/completed","SUGGESTION_DEAL_FOR_DEAL", "SUGGESTION"),

    // 建议办理
    WAIT_DEAL_SUGGESTIONS(null, "待办理",(byte)1,"WAIT_DEAL_SUGGESTIONS","unit/to_deal","SUGGESTION_DEAL_FOR_DEAL", "SUGGESTION"),
    DEALING_SUGGESTIONS(null, "办理中",(byte)1,"DEALING_SUGGESTIONS","unit/dealing","SUGGESTION_DEAL_FOR_DEAL", "SUGGESTION"),
    DEAL_DONE_SUGGESTIONS(null, "已办完",(byte)1,"DEAL_DONE_SUGGESTIONS","unit/done","SUGGESTION_DEAL_FOR_DEAL", "SUGGESTION"),
    DEAL_COMPLETED_SUGGESTIONS(null, "已办结",(byte)1,"DEAL_COMPLETED_SUGGESTIONS","unit/complete","SUGGESTION_DEAL_FOR_DEAL", "SUGGESTION"),


    //后台菜单
    //基本信息
    ACCOUNT_MANAGE("account", "账号管理",(byte)2, "menu-account", "/basic_info/account",null, "BASIC_INFO"),
    NEWS_TYPE_MANAGE("newsType", "新闻类型管理",(byte)2, "menu-studyType", "/basic_info/newsType",null, "BASIC_INFO"),
    NEWS_MANAGE("news", "新闻管理",(byte)2, "menu-news", "/basic_info/news",null, "BASIC_INFO"),
    NPC_MANAGE("members", "代表管理",(byte)2, "menu-performance", "/basic_info/members",null, "BASIC_INFO"),
    STUDY_MANAGE("studyFile", "学习资料管理",(byte)2, "menu-studyFile", "/basic_info/studyFile",null, "BASIC_INFO"),
    NEWS_AUDITOR("newsAuditor", "新闻审核人设置",(byte)2, "menu-special", "/basic_info/special",null, "BASIC_INFO"),
    STUDY_TYPE_MANAGE("studyCate", "学习类型管理",(byte)2, "menu-studyType", "/basic_info/studyCate",null, "BASIC_INFO"),
    SESSION_MANAGE("sessions", "届期管理",(byte)2, "menu-worktime", "/basic_info/sessions",null, "BASIC_INFO"),
    TOWN_MANAGE("towns", "镇/街道管理",(byte)2, "menu-town", "/basic_info/towns",null, "BASIC_INFO"),
    VILLAGE_MANAGE("villages", "村管理",(byte)2, "menu-village", "/basic_info/villages",null, "BASIC_INFO"),


    //代表之家,
    HOMEPAGE("index", "首页",(byte)2, "menu-dashboard", "/member_house/index",null, "MEMBER_HOUSE"),
    NOTICE_MANAGE("notification", "通知管理",(byte)2, "menu-notify", "/member_house/notification",null, "MEMBER_HOUSE"),
    OPINION_MANAGE("opinions", "选民意见管理",(byte)2, "menu-opinion", "/member_house/opinions",null, "MEMBER_HOUSE"),
    SUGGESTION_MANAGE("suggestion", "代表建议管理",(byte)2, "menu-suggestion", "/member_house/suggestion",null, "MEMBER_HOUSE"),

    TOWN_SUGGESTION_MANAGE("townSuggestion", "各镇代表建议管理",(byte)2, "menu-suggestion", "/member_house/townSuggestion",null, "MEMBER_HOUSE"),
    PERFORMANCE_MANAGE("performance", "代表履职管理",(byte)2, "menu-performanceDuty", "/member_house/performance",null, "MEMBER_HOUSE"),
    TOWN_PERFORMANCE_MANAGE("townPerformance", "各镇代表履职管理",(byte)2, "menu-performanceDuty", "/member_house/townPerformance",null, "MEMBER_HOUSE"),
    PERMISSION_MANAGE("special", "代表权限管理",(byte)2, "menu-special", "/member_house/special",null, "MEMBER_HOUSE"),
    SYSTEM_SETTING("systemSetting", "系统设置",(byte)2, "menu-setting", "/member_house/systemSetting",null, "MEMBER_HOUSE"),

    TYPE_MANAGE("typeManage", "类型管理",(byte)2, null, null,null, "MEMBER_HOUSE"),
    SUGGESTION_TYPE_MANAGE("suggestionBusiness", "建议类型管理",(byte)2, "menu-suggestionType", "/member_house/suggestionBusiness","TYPE_MANAGE", "MEMBER_HOUSE"),
    PERFORMANCE_TYPE_MANAGE("performanceType", "履职类型管理",(byte)2, "menu-performanceType", "/member_house/performanceType","TYPE_MANAGE", "MEMBER_HOUSE"),

    BASIC_MANAGE("basicInfo", "基本信息管理",(byte)2, null, null,null, "MEMBER_HOUSE"),
    NPC_MEMBER_GROUP("groups", "代表小组管理",(byte)2, "menu-group", "/member_house/groups","BASIC_MANAGE", "MEMBER_HOUSE"),
    WORKSTATION_MANAGE("workStation", "工作站管理",(byte)2, "menu-workstation", "/member_house/workStation","BASIC_MANAGE", "MEMBER_HOUSE"),

    STATISTICS_MANAGE("statisticsManage", "统计",(byte)2, null, null,null, "MEMBER_HOUSE"),
    SUGGESTION_COUNT("suggestionCount", "代表建议统计",(byte)2, "menu-statistics", "/member_house/suggestion_count","STATISTICS_MANAGE", "MEMBER_HOUSE"),
    PERFORMANCE_COUNT("performanceCount", "代表履职统计",(byte)2, "menu-statisics2", "/member_house/performanceCount","STATISTICS_MANAGE", "MEMBER_HOUSE"),
    TOWN_PERFORMANCE_COUNT("townPerformanceCount", "各镇履职统计",(byte)2, "menu-statisics2", "/member_house/townPerformanceCount","STATISTICS_MANAGE", "MEMBER_HOUSE"),

    //建议办理
    //人大后台管理员
    NPC_HOMEPAGE_DEAL("npcDealIndex", "首页",(byte)2, "sug-dealIndex", "/suggestion_deal/npc/npcDealIndex",null, "SUGGESTION"),
    GOVERNMENT_MANAGE("govManage", "政府管理",(byte)2, "sug-govManage", "/suggestion_deal/npc/govManage",null, "SUGGESTION"),
    SUGGESTION_TYPE_DEAL("suggestionBusinessDeal", "建议类型管理",(byte)2, "sug-suggestionBusiness", "/suggestion_deal/npc/suggestionBusiness",null, "SUGGESTION"),
    SUGGESTION_DEAL("suggestionManage", "代表建议管理",(byte)2, "sug-suggestionManage", "/suggestion_deal/npc/suggestion",null, "SUGGESTION"),
    SUGGESTION_RECEIVER_MANAGE("sugReceiver", "建议接收人设置",(byte)2, "menu-opinion", "/suggestion_deal/npc/sugReceiver",null, "SUGGESTION"),

    //政府
    GOV_HOMEPAGE_DEAL("govDealIndex", "首页",(byte)2, "sug-dealIndex", "/suggestion_deal/govDealIndex",null, "SUGGESTION"),
    GOV_WAIT_CONVEY("toBeTransSug", "待转办的建议",(byte)2, "sug-toBeTransSug", "/suggestion_deal/gov/toBeTransSug",null, "SUGGESTION"),
    GOV_ADJUST_CONVEY("applyAdjustSug", "申请调整的建议",(byte)2, "sug-applyAdjustSug", "/suggestion_deal/gov/applyAdjustSug",null, "SUGGESTION"),
    GOV_ADJUST_DELAY("applyDelaySug", "申请延期的建议",(byte)2, "sug-applyDelaySug", "/suggestion_deal/gov/applyDelaySug",null, "SUGGESTION"),
    GOV_DEALING("govDealingSug", "办理中的建议",(byte)2, "sug-inDoingSug", "/suggestion_deal/gov/dealingSug",null, "SUGGESTION"),
    GOV_FINISHED("govFinishedSug", "办理完成的建议",(byte)2, "sug-doneSug", "/suggestion_deal/gov/finishedSug",null, "SUGGESTION"),
    GOV_COMPLETED("govCompletedSug", "办结的建议",(byte)2, "sug-completedSug", "/suggestion_deal/gov/completedSug",null, "SUGGESTION"),
    UNIT_MANAGE("unitManage", "办理单位管理",(byte)2, "sug-unitManage", "/suggestion_deal/gov/unitManage",null, "SUGGESTION"),
    SUGGESTION_SETTING("sugDealSetting", "建议办理设置",(byte)2, "sug-sugDealSetting", "/suggestion_deal/gov/sugDealSetting",null, "SUGGESTION"),
//    GOV_COUNT("sugDealStatistic", "建议办理统计",(byte)2, "sug-sugDealStatistic", "/suggestion_deal/gov/sugDealStatistic",null, "SUGGESTION"),

    //办理单位
    UNIT_HOMEPAGE_DEAL("unitDealIndex", "首页",(byte)2, "sug-dealIndex", "/suggestion_deal/unitDealIndex",null, "SUGGESTION"),
    UNIT_WAIT_DEAL("toBeDoneSug", "待办建议",(byte)2, "sug-toBeDoneSug", "/suggestion_deal/unit/toBeDoneSug",null, "SUGGESTION"),
    UNIT_DEALING("inDoingSug", "办理中",(byte)2, "sug-inDoingSug", "/suggestion_deal/unit/inDoingSug",null, "SUGGESTION"),
    UNIT_DEAL_DONE("doneSug", "办理完成",(byte)2, "sug-doneSug", "/suggestion_deal/unit/doneSug",null, "SUGGESTION"),
    UNIT_DEAL_COMPLETED("completedSug", "办结",(byte)2, "sug-completedSug", "/suggestion_deal/unit/completedSug",null, "SUGGESTION");



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
