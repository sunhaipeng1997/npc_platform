package com.cdkhd.npc.enums;

public enum NpcMemberRoleEnum {
    MEMBER("普通代表",true),
    CHAIRMAN("人大主席",true),
    SPECIAL_MAN("特殊人员",true),
    NEWS_AUDITOR("新闻审核人员",false),
    NOTICE_AUDITOR("通知公告审核人员",false),
    SUGGESTION_RECEIVER("建议接收人员",false),
    PERFORMANCE_AUDITOR("履职小组审核人员",false),
    PERFORMANCE_GENERAL_AUDITOR("履职总审核人员",false);

    private String name;
    private Boolean isMust;

    NpcMemberRoleEnum(String name, Boolean isMust) {
        this.name = name;
        this.isMust = isMust;
    }


    public String getKeyword() {
        return this.name();
    }

    public String getName() {
        return name;
    }

    public Boolean getIsMust() {
        return isMust;
    }

}
