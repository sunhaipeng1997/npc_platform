package com.cdkhd.npc.enums;

public enum SuggestionStatusEnum {
    /**
     * ---------建议状态
     已撤回	0
     未提交	1
     已提交审核	2
     已提交政府	3
     已转交办理单位	4
     办理中	5
     办理完成	6
     办结	7
     自行办理	8
     审核失败	-1
     */
    HAS_BEEN_REVOKE((byte)0, "已撤回"),
    NOT_SUBMITTED((byte)1, "未提交"),
    SUBMITTED_AUDIT((byte)2, "待审核"),
    SUBMITTED_GOVERNMENT((byte)3, "已提交政府"),
    TRANSFERRED_UNIT((byte)4, "已转交办理单位"),
    HANDLING((byte)5, "办理中"),
    HANDLED((byte)6, "办理完成"),
    ACCOMPLISHED((byte)7, "办结"),
    SELF_HANDLE((byte)8, "自行办理"),
    AUDIT_FAILURE((byte)-1, "审核失败");

    private Byte value;
    private String name;

    SuggestionStatusEnum(Byte value, String name){
        this.value = value;
        this.name = name;
    }

    public Byte getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static Byte getValue(String name) {
        SuggestionStatusEnum[] suggestionStatusEnums = values();
        for (SuggestionStatusEnum suggestionStatusEnum : suggestionStatusEnums) {
            if (suggestionStatusEnum.getName().equals(name)) {
                return suggestionStatusEnum.getValue();
            }
        }
        return null;
    }

    public static String getName(Byte value) {
        SuggestionStatusEnum[] suggestionStatusEnums = values();
        for (SuggestionStatusEnum suggestionStatusEnum : suggestionStatusEnums) {
            if (suggestionStatusEnum.getValue().equals(value)) {
                return suggestionStatusEnum.getName();
            }
        }
        return null;
    }
}
