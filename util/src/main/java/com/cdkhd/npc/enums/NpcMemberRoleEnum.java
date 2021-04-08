package com.cdkhd.npc.enums;

public enum NpcMemberRoleEnum {
    MEMBER("代表",true, false),
    CHAIRMAN("人大领导",true, false),
    SPECIAL_MAN("办公室人员",true, true),
    NEWS_AUDITOR("新闻审核人员",false, false),
    NOTICE_AUDITOR("通知公告审核人员",false, false),
    SUGGESTION_RECEIVER("建议接收人员",false, false),
    PERFORMANCE_AUDITOR("履职小组审核人员",false, false),
    PERFORMANCE_GENERAL_AUDITOR("履职总审核人员",false, false);

    private String name;
    private Boolean isMust;
    private Boolean special;

    NpcMemberRoleEnum(String name, Boolean isMust, Boolean special) {
        this.name = name;
        this.isMust = isMust;
        this.special = special;
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

    public Boolean getSpecial() {
        return special;
    }
}
