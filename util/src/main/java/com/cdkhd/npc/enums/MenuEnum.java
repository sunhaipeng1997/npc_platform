package com.cdkhd.npc.enums;

public enum MenuEnum {
    //小程序菜单类型
    COMMUNICATION("互动交流",(byte)1,null,null),//name   type   url   parent
    NOTIFICATION("通知公告",(byte)1,null,null),
    STATISTICAL_RANKING("排名统计",(byte)1,null,null),
    SPECIAL_FUNCTIONS("特殊职能",(byte)1,null,null),
    //小程序菜单
    //互动交流
    MEMBER_INFO("代表风采",(byte)1,"","COMMUNICATION"),
    WORK_STATION("工作站",(byte)1,"","COMMUNICATION"),
    MY_OPINION("我的意见",(byte)1,"","COMMUNICATION"),
    MY_SUGGESTION("我的建议",(byte)1,"","COMMUNICATION"),
    RECEIVE_OPINION("收到意见",(byte)1,"","SPECIAL_FUNCTIONS"),
    MY_PERFORMANCE("我的履职",(byte)1,"","COMMUNICATION"),
//    SECONDED_SUGGESTION("我的附议",(byte)1,"","COMMUNICATION"),

    //通知公告
    NOTIFICATION_INFO("通知信息",(byte)1,"","NOTIFICATION"),
    ANNOUNCEMENT("公告信息",(byte)1,"","NOTIFICATION"),

    //统计排名
    MEMBER_RANK("代表排名",(byte)1,"","COMMUNICATION"),
    TOWN_RANK("各镇排名",(byte)1,"","COMMUNICATION"),

    //特殊职能
    AUDIT_NEWS("新闻审核",(byte)1,"","SPECIAL_FUNCTIONS"),
    AUDIT_SUGGESTION("审核建议",(byte)1,"","COMMUNICATION"),
    AUDIT_PERFORMANCE("履职审核",(byte)1,"","SPECIAL_FUNCTIONS"),
    AUDIT_NOTICE("通知审核",(byte)1,"","SPECIAL_FUNCTIONS"),

    //后台菜单
    ACCOUNT_MANAGE("账号管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    STUDY_MANAGE("学习资料管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    WORKSTATION_MANAGE("联络点管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    NEWS_MANAGE("新闻管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    NOTICE_MANAGE("通知管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    NPC_MANAGE("代表管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    TOWN_MANAGE("镇管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    GROUP_MANAGE("小组管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    VILLAGE_MANAGE("村管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    OPINION_MANAGE("选民意见管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    SUGGESTION_MANAGE("代表建议管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    PERFORMANCE_MANAGE("代表履职管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    STATISTICS("统计分析",(byte)2,"","SPECIAL_FUNCTIONS"),
    PERMISSION_MANAGE("代表权限管理",(byte)2,"","SPECIAL_FUNCTIONS"),
    SESSION_MANAGE("届期管理",(byte)2,"","SPECIAL_FUNCTIONS");

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
