package com.cdkhd.npc.enums;

public enum MenuEnum {
    //小程序菜单类型
    COMMUNICATION("互动交流",(byte)1,null,null),//name   type   url   parent
    NOTIFICATION("通知公告",(byte)1,null,null),
    STATISTICAL_RANKING("排名统计",(byte)1,null,null),
    SPECIAL_FUNCTIONS("特殊职能",(byte)1,null,null),
    //小程序菜单
    //互动交流
    MEMBER_INFO("代表风采",(byte)1,null,"COMMUNICATION"),
    WORK_STATION("工作站",(byte)1,null,"COMMUNICATION"),
    MY_OPINION("我的意见",(byte)1,null,"COMMUNICATION"),
    MY_SUGGESTION("我的建议",(byte)1,null,"COMMUNICATION"),
    RECEIVE_OPINION("收到意见",(byte)1,null,"SPECIAL_FUNCTIONS"),
    MY_PERFORMANCE("我的履职",(byte)1,null,"COMMUNICATION"),
//    SECONDED_SUGGESTION("我的附议",(byte)1,null,"COMMUNICATION"),

    //通知公告
    NOTIFICATION_INFO("通知信息",(byte)1,null,"NOTIFICATION"),
    ANNOUNCEMENT("公告信息",(byte)1,null,"NOTIFICATION"),

    //统计排名
    MEMBER_RANK("代表排名",(byte)1,null,"COMMUNICATION"),
    TOWN_RANK("各镇排名",(byte)1,null,"COMMUNICATION"),

    //特殊职能
    AUDIT_NEWS("新闻审核",(byte)1,null,"SPECIAL_FUNCTIONS"),
    AUDIT_SUGGESTION("审核建议",(byte)1,null,"COMMUNICATION"),
    AUDIT_PERFORMANCE("履职审核",(byte)1,null,"SPECIAL_FUNCTIONS"),
    AUDIT_NOTICE("通知审核",(byte)1,null,"SPECIAL_FUNCTIONS"),

    //后台菜单
    //类型管理
    TYPE_MANAGE("类型管理",(byte)2,null,null),
    BASIC_MANAGE("基本信息管理",(byte)2,null,null),

    ACCOUNT_MANAGE("账号管理",(byte)2,"/member_house/account",null),
    NEWS_MANAGE("新闻管理",(byte)2,"/member_house/news",null),
    NOTICE_MANAGE("通知管理",(byte)2,"/member_house/notification",null),
    NPC_MANAGE("代表管理",(byte)2,"/member_house/members",null),
    NPC_MEMBER_GROUP("代表小组管理",(byte)2,"/member_house/groups",null),
    WORKSTATION_MANAGE("工作站管理",(byte)2,"/member_house/workStation",null),
    OPINION_MANAGE("选民意见管理",(byte)2,"/member_house/opinions",null),
    STUDY_TYPE_MANAGE("学习类型管理",(byte)2,"/member_house/studyCate",null),
    STUDY_MANAGE("学习资料管理",(byte)2,"/member_house/studyFile",null),
    SUGGESTION_TYPE_MANAGE("建议类型管理",(byte)2,"/member_house/suggestionBusiness",null),
    SUGGESTION_MANAGE("代表建议管理",(byte)2,"/member_house/suggestion",null),
    PERFORMANCE_TYPE_MANAGE("履职类型管理",(byte)2,"/member_house/performanceType",null),
    PERFORMANCE_MANAGE("代表履职管理",(byte)2,"/member_house/performance",null),
    SUGGESTION_COUNT("代表建议统计",(byte)2,"/member_house/suggestion_count",null),
    PERFORMANCE_COUNT("代表履职统计",(byte)2,"/member_house/performance_count",null),
    PERMISSION_MANAGE("代表权限管理",(byte)2,"/member_house/special",null),
    SESSION_MANAGE("届期管理",(byte)2,"/member_house/sessions",null),
    TOWN_MANAGE("镇管理",(byte)2,"/member_house/towns",null),
    VILLAGE_MANAGE("村管理",(byte)2,"/member_house/villages",null);

    private String name;
    private Byte type;
    private String url;
    private String parentId;

    MenuEnum(String name, Byte type, String url, String parentId) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.parentId = parentId;
    }

    public String getName() {
        return this.name;
    }

    public Byte getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getParentId() {
        return parentId;
    }
}
